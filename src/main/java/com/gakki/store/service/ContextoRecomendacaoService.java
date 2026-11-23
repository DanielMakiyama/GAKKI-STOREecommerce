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
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

// RNF0044 — monta as instruções de sistema da IA. O backend filtra (estoque,
// orçamento, categorias, o que já foi comprado) e a IA só escolhe e explica:
// modelo pequeno erra menos quando não precisa filtrar sozinho.
// O ajuste fino (etapa 6) usa o mesmo formato.

@Service
@RequiredArgsConstructor
public class ContextoRecomendacaoService {

    // Vendas que contam como compra feita (as mesmas da análise de vendas).
    static final List<String> STATUS_COMPRADO = List.of(
            StatusPedido.APROVADA.name(), StatusPedido.EM_TRANSPORTE.name(), StatusPedido.ENTREGUE.name());

    static final String FUSO_DA_LOJA = "America/Sao_Paulo";

    private static final int MAXIMO_SUGESTOES = 8;

    private static final Locale PT_BR = Locale.of("pt", "BR");

    // Poucas regras e um exemplo: modelos pequenos imitam melhor do que obedecem listas.
    private static final String REGRAS = """
            Você é o assistente de compras da GAKKI STORE, loja de instrumentos musicais.
            Responda em português do Brasil, com cordialidade, em até 3 frases.
            Regras:
            1. Fale só de produtos do CATÁLOGO, com o código entre colchetes. Não invente produtos nem preços.
            2. Para recomendar, escolha UM produto das SUGESTÕES PARA ESTE CLIENTE e diga em uma frase por que combina com ele.
            3. Fora de música e da loja, diga que só ajuda com esses assuntos.
            Exemplo de recomendação:
            Recomendo o Pedal Ibanez TS9 Tube Screamer [INST-0023], por R$ 840,00. Ele combina com o seu blues rock e soma bem ao pedal de distorção que você já tem.
            """;

    private final PreferenciaClienteRepository preferenciaRepository;
    private final HistoricoComprasRepository historicoRepository;
    private final InstrumentoRepository instrumentoRepository;

    @Transactional(readOnly = true)
    public String instrucoesDoSistema(Long clienteId) {
        Optional<PreferenciaCliente> preferencia = preferenciaRepository.findByClienteId(clienteId);
        List<ItemComprado> compras = historicoRepository.itensDoCliente(clienteId, STATUS_COMPRADO, FUSO_DA_LOJA);
        List<Instrumento> catalogo = instrumentoRepository.findAllByAtivoTrueOrderByNomeAsc();

        // Parte igual para todos os clientes primeiro: o Ollama reaproveita o
        // que já leu, e só o trecho do cliente é processado a cada pergunta.
        return gerais(catalogo)
                + "\nPREFERÊNCIAS DO CLIENTE:\n" + secaoPreferencias(preferencia)
                + "\nJÁ COMPROU:\n" + secaoHistorico(compras)
                + "\nSUGESTÕES PARA ESTE CLIENTE:\n" + secaoSugestoes(catalogo, preferencia, compras);
    }

    // Regras e catálogo, iguais para todos. O aquecimento da IA lê só isto.
    @Transactional(readOnly = true)
    public String instrucoesGerais() {
        return gerais(instrumentoRepository.findAllByAtivoTrueOrderByNomeAsc());
    }

    private String gerais(List<Instrumento> catalogo) {
        return REGRAS + "\nCATÁLOGO:\n" + secaoCatalogo(catalogo);
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

    // Só os nomes, sem repetir: é o que a IA usa para "combina com o que você já tem".
    String secaoHistorico(List<ItemComprado> compras) {
        if (compras.isEmpty()) {
            return "Nada ainda.\n";
        }
        return compras.stream()
                .map(item -> "[" + item.getCodigo() + "] " + item.getNome())
                .distinct()
                .collect(Collectors.joining("; ", "", "\n"));
    }

    // Filtro feito pelo sistema: em estoque, dentro do orçamento, nas categorias
    // de interesse (ou nas do histórico) e ainda não comprado. Os mais caros
    // primeiro, para os instrumentos aparecerem antes dos acessórios baratos.
    String secaoSugestoes(List<Instrumento> catalogo, Optional<PreferenciaCliente> preferencia,
                          List<ItemComprado> compras) {
        Set<String> comprados = compras.stream().map(ItemComprado::getCodigo).collect(Collectors.toSet());
        Set<String> interesse = categoriasDeInteresse(preferencia, compras);
        BigDecimal teto = preferencia.map(PreferenciaCliente::getOrcamentoMaximo).orElse(null);

        List<Instrumento> sugestoes = catalogo.stream()
                .filter(ContextoRecomendacaoService::temEstoque)
                .filter(i -> !comprados.contains(i.getCodigo()))
                .filter(i -> teto == null || i.getValorVenda().compareTo(teto) <= 0)
                .filter(i -> interesse.isEmpty() || relevancia(i, interesse) > 0)
                .sorted(Comparator.comparing((Instrumento i) -> relevancia(i, interesse)).reversed()
                        .thenComparing(Instrumento::getValorVenda, Comparator.reverseOrder()))
                .limit(MAXIMO_SUGESTOES)
                .toList();

        if (sugestoes.isEmpty()) {
            return "Nenhuma sugestão automática; use o CATÁLOGO.\n";
        }
        return sugestoes.stream()
                .map(i -> "- [%s] %s, %s".formatted(i.getCodigo(), i.getNome(), moeda(i.getValorVenda())))
                .collect(Collectors.joining("\n", "", "\n"));
    }

    // Agrupado pela primeira categoria do produto, uma linha curta por item.
    String secaoCatalogo(List<Instrumento> catalogo) {
        Map<String, List<Instrumento>> porCategoria = catalogo.stream()
                .collect(Collectors.groupingBy(ContextoRecomendacaoService::categoriaPrincipal,
                        TreeMap::new, Collectors.toList()));

        StringBuilder texto = new StringBuilder();
        porCategoria.forEach((categoria, itens) -> {
            texto.append(categoria).append(":\n");
            itens.forEach(instrumento -> texto.append("- [").append(instrumento.getCodigo()).append("] ")
                    .append(instrumento.getNome()).append(", ")
                    .append(moeda(instrumento.getValorVenda()))
                    .append(temEstoque(instrumento) ? "" : " (esgotado)")
                    .append('\n'));
        });
        return texto.toString();
    }

    private static Set<String> categoriasDeInteresse(Optional<PreferenciaCliente> preferencia,
                                                     List<ItemComprado> compras) {
        Set<String> daPreferencia = preferencia
                .map(p -> p.getCategorias().stream().map(Categoria::getNome).collect(Collectors.toSet()))
                .orElse(Set.of());
        if (!daPreferencia.isEmpty()) {
            return daPreferencia;
        }
        return compras.stream()
                .filter(item -> item.getCategorias() != null)
                .flatMap(item -> Arrays.stream(item.getCategorias().split(",\\s*")))
                .collect(Collectors.toSet());
    }

    // +1 por categoria de interesse, -1 por categoria fora dele (ex.: cordas de
    // guitarra são "Acessórios" e "Guitarras"; para quem só quer acessórios, saem).
    private static int relevancia(Instrumento instrumento, Set<String> interesse) {
        return instrumento.getCategorias().stream()
                .mapToInt(c -> interesse.contains(c.getNome()) ? 1 : -1)
                .sum();
    }

    private static boolean temEstoque(Instrumento instrumento) {
        Integer estoque = instrumento.getQuantidadeEstoque();
        return estoque != null && estoque > 0;
    }

    private static String categoriaPrincipal(Instrumento instrumento) {
        return instrumento.getCategorias().stream()
                .map(Categoria::getNome).sorted().findFirst().orElse("Outros");
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
