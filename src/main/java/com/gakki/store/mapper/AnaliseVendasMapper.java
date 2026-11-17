package com.gakki.store.mapper;

import com.gakki.store.domain.vendas.Categoria;
import com.gakki.store.dto.response.CategoriaResponse;
import com.gakki.store.dto.response.HistoricoVendasResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

// Conversões da análise de vendas (RF0055).

@Component
public class AnaliseVendasMapper {

    private static final DateTimeFormatter FORMATO_MES = DateTimeFormatter.ofPattern("MM/yyyy");

    public CategoriaResponse paraResponse(Categoria categoria) {
        return new CategoriaResponse(categoria.getId(), categoria.getNome());
    }

    public HistoricoVendasResponse paraResponse(String categoria, YearMonth mes, BigDecimal totalVendas) {
        return new HistoricoVendasResponse(categoria, mes.format(FORMATO_MES), totalVendas);
    }
}
