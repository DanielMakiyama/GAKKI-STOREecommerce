package com.gakki.store.service;

import com.gakki.store.domain.vendas.Categoria;
import com.gakki.store.domain.vendas.enums.StatusPedido;
import com.gakki.store.dto.response.CategoriaResponse;
import com.gakki.store.dto.response.HistoricoVendasResponse;
import com.gakki.store.exception.RegraDeNegocioException;
import com.gakki.store.mapper.AnaliseVendasMapper;
import com.gakki.store.repository.AnaliseVendasRepository;
import com.gakki.store.repository.AnaliseVendasRepository.VendaMensal;
import com.gakki.store.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/*
 * Análise do histórico de vendas por categoria (RF0055, RNF0043, RNF0011).
 *
 * Critérios do enunciado, e onde cada um é aplicado:
 *
 *   (a) comparação entre categorias  -> a lista de categorias escolhidas
 *   (b) só o administrador            -> @PreAuthorize no controller
 *   (c) eixo agrupado por mês         -> o mês da compra, no fuso da loja
 *   (d) só compras ENTREGUE           -> o status passado à consulta
 *   (e) valor total em reais          -> soma de quantidade × valor_unitario
 *
 * O mês de uma venda é o mês da COMPRA (pedido.criado_em), e não o da
 * entrega, que o pedido não grava. Uma compra entra no gráfico quando
 * chega a ENTREGUE, no mês em que foi feita.
 *
 * Sem cache, de propósito: o cenário 6 do roteiro pede que uma compra que
 * passa a ENTREGUE apareça na consulta seguinte. Com a agregação no
 * banco e os índices da V17, recalcular a cada consulta cabe com folga
 * no 1 segundo do RNF0011.
 */
@Service
@RequiredArgsConstructor
public class AnaliseVendasService {

    // O fuso da loja decide em que dia e em que mês uma compra caiu. Vale
    // para os dois lados: o período informado na tela e o agrupamento no
    // banco usam o mesmo, senão uma compra das 23h do dia 30 escaparia do
    // período ou cairia no mês seguinte.
    static final ZoneId FUSO_DA_LOJA = ZoneId.of("America/Sao_Paulo");

    // Escala 2, igual à dos valores que vêm do banco: o JSON mostra 0.00
    // nos meses sem venda, no mesmo formato dos outros pontos.
    private static final BigDecimal SEM_VENDA = new BigDecimal("0.00");

    private final AnaliseVendasRepository analiseVendasRepository;
    private final CategoriaRepository categoriaRepository;
    private final AnaliseVendasMapper analiseVendasMapper;

    /** Opções da tela, em ordem alfabética. */
    @Transactional(readOnly = true)
    public List<CategoriaResponse> listarCategorias() {
        return categoriaRepository.findAllByOrderByNomeAsc().stream()
                .map(analiseVendasMapper::paraResponse)
                .toList();
    }

    /*
     * RF0055 — o histórico do período para as categorias escolhidas.
     *
     * Devolve uma linha para CADA par (categoria, mês) do período, mesmo
     * sem venda: um mês ausente seria desenhado como uma linha ligando
     * direto os meses vizinhos, sugerindo vendas que não aconteceram. O
     * zero tem de aparecer no gráfico.
     *
     * O período vale por dia, não por mês cheio: de 15/04 a 10/05, abril
     * soma só a partir do dia 15 e maio só até o dia 10. É o que "busca
     * por período, com data de início e de fim" diz, e o que permite ver o
     * gráfico mudar ao alterar qualquer uma das duas datas (cenário 4).
     */
    @Transactional(readOnly = true)
    public List<HistoricoVendasResponse> historico(LocalDate inicio,
                                                   LocalDate fim,
                                                   List<String> nomesDasCategorias) {
        validarPeriodo(inicio, fim);
        List<Categoria> categorias = resolverCategorias(nomesDasCategorias);

        // Intervalo semiaberto: [início do dia de início, início do dia
        // seguinte ao fim). Inclui o dia de fim inteiro, até 23:59:59,999.
        OffsetDateTime de = inicio.atStartOfDay(FUSO_DA_LOJA).toOffsetDateTime();
        OffsetDateTime ateExclusivo = fim.plusDays(1).atStartOfDay(FUSO_DA_LOJA).toOffsetDateTime();

        List<VendaMensal> vendas = analiseVendasRepository.totaisMensaisPorCategoria(
                StatusPedido.ENTREGUE.name(),
                FUSO_DA_LOJA.getId(),
                de,
                ateExclusivo,
                categorias.stream().map(Categoria::getId).toList());

        Map<String, Map<YearMonth, BigDecimal>> vendidoPorCategoria = new HashMap<>();
        for (VendaMensal venda : vendas) {
            vendidoPorCategoria
                    .computeIfAbsent(venda.getCategoria(), nome -> new HashMap<>())
                    .put(YearMonth.parse(venda.getMes()), venda.getTotal());
        }

        List<YearMonth> meses = mesesEntre(YearMonth.from(inicio), YearMonth.from(fim));

        List<HistoricoVendasResponse> historico = new ArrayList<>();
        for (Categoria categoria : categorias) {
            Map<YearMonth, BigDecimal> vendido = vendidoPorCategoria.getOrDefault(categoria.getNome(), Map.of());
            for (YearMonth mes : meses) {
                historico.add(analiseVendasMapper.paraResponse(
                        categoria.getNome(), mes, vendido.getOrDefault(mes, SEM_VENDA)));
            }
        }
        return historico;
    }

    private void validarPeriodo(LocalDate inicio, LocalDate fim) {
        if (inicio == null || fim == null) {
            throw new RegraDeNegocioException("Informe a data de início e a data de fim do período.");
        }
        // Fim antes do início é período vazio: devolver um gráfico sem
        // pontos esconderia o erro de digitação em vez de apontá-lo.
        if (fim.isBefore(inicio)) {
            throw new RegraDeNegocioException("A data de fim não pode ser anterior à data de início.");
        }
    }

    /*
     * Resolve os nomes enviados pela tela em categorias cadastradas.
     *
     * Ao menos uma é obrigatória: sem categoria não há o que comparar, e
     * a consulta com `IN ()` vazio seria SQL inválido. Um nome que não
     * existe é recusado com o nome na mensagem — ignorá-lo em silêncio
     * mostraria um gráfico sem a linha pedida, sem dizer por quê.
     *
     * A saída vem em ordem alfabética, independente da ordem em que a tela
     * marcou as categorias: a legenda e as cores do gráfico ficam estáveis
     * entre uma consulta e outra.
     */
    private List<Categoria> resolverCategorias(List<String> nomesDasCategorias) {
        List<String> pedidas = nomesDasCategorias == null ? List.of() : nomesDasCategorias.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(nome -> !nome.isEmpty())
                .distinct()
                .toList();

        if (pedidas.isEmpty()) {
            throw new RegraDeNegocioException("Selecione ao menos uma categoria para a análise.");
        }

        List<Categoria> encontradas = categoriaRepository.findByNomeIn(pedidas);

        if (encontradas.size() != pedidas.size()) {
            List<String> conhecidas = encontradas.stream().map(Categoria::getNome).toList();
            List<String> desconhecidas = pedidas.stream().filter(nome -> !conhecidas.contains(nome)).toList();
            throw new RegraDeNegocioException(
                    "Categoria não cadastrada: %s.".formatted(String.join(", ", desconhecidas)));
        }

        return encontradas.stream()
                .sorted(Comparator.comparing(Categoria::getNome))
                .toList();
    }

    // Todos os meses do período, do primeiro ao último, inclusive.
    private List<YearMonth> mesesEntre(YearMonth primeiro, YearMonth ultimo) {
        List<YearMonth> meses = new ArrayList<>();
        for (YearMonth mes = primeiro; !mes.isAfter(ultimo); mes = mes.plusMonths(1)) {
            meses.add(mes);
        }
        return meses;
    }
}
