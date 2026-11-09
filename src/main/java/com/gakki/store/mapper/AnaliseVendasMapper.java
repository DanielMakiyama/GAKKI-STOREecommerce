package com.gakki.store.mapper;

import com.gakki.store.domain.vendas.Categoria;
import com.gakki.store.dto.response.CategoriaResponse;
import com.gakki.store.dto.response.HistoricoVendasResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

/*
 * Conversões da análise de vendas (RF0055). Sem repository injetado,
 * como todos os mappers do projeto.
 */
@Component
public class AnaliseVendasMapper {

    // MM/AAAA, o formato que o mock do front devolvia e que o eixo
    // horizontal do gráfico mostra.
    private static final DateTimeFormatter FORMATO_MES = DateTimeFormatter.ofPattern("MM/yyyy");

    public CategoriaResponse paraResponse(Categoria categoria) {
        return new CategoriaResponse(categoria.getId(), categoria.getNome());
    }

    public HistoricoVendasResponse paraResponse(String categoria, YearMonth mes, BigDecimal totalVendas) {
        return new HistoricoVendasResponse(categoria, mes.format(FORMATO_MES), totalVendas);
    }
}
