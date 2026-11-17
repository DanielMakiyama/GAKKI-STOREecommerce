package com.gakki.store.dto.response;

import java.math.BigDecimal;

// RF0055 e RNF0043 - um ponto do gráfico: quanto uma categoria vendeu num mês.

public record HistoricoVendasResponse(
        String categoria,
        String mesAno,
        BigDecimal totalVendas) {
}
