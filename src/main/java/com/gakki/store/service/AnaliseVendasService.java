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

// Análise do histórico de vendas por categoria (RF0055, RNF0043, RNF0011).

@Service
@RequiredArgsConstructor
public class AnaliseVendasService {

    static final ZoneId FUSO_DA_LOJA = ZoneId.of("America/Sao_Paulo");
    private static final BigDecimal SEM_VENDA = new BigDecimal("0.00");
    private final AnaliseVendasRepository analiseVendasRepository;
    private final CategoriaRepository categoriaRepository;
    private final AnaliseVendasMapper analiseVendasMapper;

    @Transactional(readOnly = true)
    public List<CategoriaResponse> listarCategorias() {
        return categoriaRepository.findAllByOrderByNomeAsc().stream()
                .map(analiseVendasMapper::paraResponse)
                .toList();
    }

    // RF0055 - o histórico do período para as categorias escolhidas.

    @Transactional(readOnly = true)
    public List<HistoricoVendasResponse> historico(LocalDate inicio,
                                                   LocalDate fim,
                                                   List<String> nomesDasCategorias) {
        validarPeriodo(inicio, fim);
        List<Categoria> categorias = resolverCategorias(nomesDasCategorias);

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
        if (fim.isBefore(inicio)) {
            throw new RegraDeNegocioException("A data de fim não pode ser anterior à data de início.");
        }
    }

    // Resolve os nomes enviados pela tela em categorias cadastradas.

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
