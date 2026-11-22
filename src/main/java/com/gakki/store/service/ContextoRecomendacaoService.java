package com.gakki.store.service;

import com.gakki.store.domain.recomendacao.PreferenciaCliente;
import com.gakki.store.domain.recomendacao.enums.EstiloMusical;
import com.gakki.store.domain.recomendacao.enums.NivelMusical;
import com.gakki.store.domain.vendas.Categoria;
import com.gakki.store.domain.vendas.Instrumento;
import com.gakki.store.domain.vendas.enums.StatusPedido;
import com.gakki.store.repository.HistoricoComprasRepository;
import com.gakki.store.repository.HistoricoComprasRepository.ItemComprado;
import com.gakki.store.repository.InstrumentoRepository;
import com.gakki.store.repository.PreferenciaClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

// RNF0044 — monta as instruções de sistema da IA: regras, preferências,
// histórico de compras e catálogo. O ajuste fino (etapa 6) usa o mesmo
// formato, para o modelo treinar com o que vai receber no chat.

@Service
@RequiredArgsConstructor
public class ContextoRecomendacaoService {

    // Vendas que contam como compra feita (as mesmas da análise de vendas).
    static final List<String> STATUS_COMPRADO = List.of(
            StatusPedido.APROVADA.name(), StatusPedido.EM_TRANSPORTE.name(), StatusPedido.ENTREGUE.name());

    static final String FUSO_DA_LOJA = "America/Sao_Paulo";

    private static final Locale PT_BR = Locale.of("pt", "BR");

    private static final String REGRAS = """
            Você é o assistente de compras da GAKKI STORE, loja online de instrumentos musicais.
            Responda sempre em português do Brasil, de forma cordial e curta (até 5 frases).

            Regras:
            1. Recomende somente produtos do CATÁLOGO abaixo. Nunca invente produtos, preços, prazos ou promoções.
            2. Ao citar um produto, escreva o nome e o código entre colchetes. Exemplo: Guitarra Squier Stratocaster [INST-0007].
            3. Use as PREFERÊNCIAS e o HISTÓRICO DE COMPRAS para personalizar e diga em uma frase por que o produto combina com o cliente.
            4. Não recomende de novo o que o cliente já comprou, exceto itens de reposição, como cordas e palhetas.
            5. Respeite o orçamento máximo do cliente, quando houver.
            6. Prefira produtos em estoque; se citar um esgotado, avise.
            7. Se a pergunta não for sobre música, instrumentos ou a loja, diga gentilmente que só ajuda com esses assuntos.
            """;

    private final PreferenciaClienteRepository preferenciaRepository;
    private final HistoricoComprasRepository historicoRepository;
    private final InstrumentoRepository instrumentoRepository;

    @Transactional(readOnly = true)
    public String instrucoesDoSistema(Long clienteId) {
        Optional<PreferenciaCliente> preferencia = preferenciaRepository.findByClienteId(clienteId);
        List<ItemComprado> compras = historicoRepository.itensDoCliente(clienteId, STATUS_COMPRADO, FUSO_DA_LOJA);
        List<Instrumento> catalogo = instrumentoRepository.findAllByAtivoTrueOrderByNomeAsc();

        return REGRAS
                + "\nPREFERÊNCIAS DO CLIENTE:\n" + secaoPreferencias(preferencia)
                + "\nHISTÓRICO DE COMPRAS:\n" + secaoHistorico(compras)
                + "\nCATÁLOGO (código | produto | marca | categorias | preço | estoque):\n" + secaoCatalogo(catalogo);
    }

    String secaoPreferencias(Optional<PreferenciaCliente> preferencia) {
        if (preferencia.isEmpty()) {
            return "O cliente ainda não informou preferências.\n";
        }
        PreferenciaCliente p = preferencia.get();

        String categorias = p.getCategorias().stream()
                .map(Categoria::getNome).sorted().collect(Collectors.joining(", "));
        String estilos = p.getEstilos().stream()
                .sorted().map(ContextoRecomendacaoService::rotulo).collect(Collectors.joining(", "));

        StringBuilder texto = new StringBuilder()
                .append("- Nível: ").append(rotulo(p.getNivel())).append('\n')
                .append("- Orçamento máximo: ")
                .append(p.getOrcamentoMaximo() == null ? "não informado" : moeda(p.getOrcamentoMaximo())).append('\n')
                .append("- Categorias de interesse: ").append(categorias).append('\n')
                .append("- Estilos musicais: ").append(estilos).append('\n');
        if (p.getObservacoes() != null) {
            texto.append("- Observações: ").append(p.getObservacoes()).append('\n');
        }
        return texto.toString();
    }

    String secaoHistorico(List<ItemComprado> compras) {
        if (compras.isEmpty()) {
            return "O cliente ainda não fez compras.\n";
        }
        return compras.stream()
                .map(item -> "- %s: %s [%s] (%s), %d un., %s".formatted(
                        item.getData(), item.getNome(), item.getCodigo(), item.getCategorias(),
                        item.getQuantidade(), moeda(item.getValorUnitario())))
                .collect(Collectors.joining("\n", "", "\n"));
    }

    String secaoCatalogo(List<Instrumento> catalogo) {
        return catalogo.stream()
                .map(instrumento -> {
                    Integer estoque = instrumento.getQuantidadeEstoque();
                    String categorias = instrumento.getCategorias().stream()
                            .map(Categoria::getNome).sorted().collect(Collectors.joining(", "));
                    return "[%s] | %s | %s | %s | %s | %s".formatted(
                            instrumento.getCodigo(), instrumento.getNome(), instrumento.getFabricante().getNome(),
                            categorias, moeda(instrumento.getValorVenda()),
                            estoque != null && estoque > 0 ? "em estoque" : "esgotado");
                })
                .collect(Collectors.joining("\n", "", "\n"));
    }

    private static String moeda(BigDecimal valor) {
        return String.format(PT_BR, "R$ %,.2f", valor);
    }

    private static String rotulo(NivelMusical nivel) {
        return switch (nivel) {
            case INICIANTE -> "iniciante";
            case INTERMEDIARIO -> "intermediário";
            case AVANCADO -> "avançado";
        };
    }

    private static String rotulo(EstiloMusical estilo) {
        return switch (estilo) {
            case ROCK -> "rock";
            case BLUES -> "blues";
            case METAL -> "metal";
            case JAZZ -> "jazz";
            case POP -> "pop";
            case MPB -> "MPB";
            case FUNK -> "funk";
            case SERTANEJO -> "sertanejo";
            case GOSPEL -> "gospel";
            case ELETRONICA -> "eletrônica";
            case CLASSICA -> "clássica";
        };
    }
}
