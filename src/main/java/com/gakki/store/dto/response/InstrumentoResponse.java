package com.gakki.store.dto.response;

import java.math.BigDecimal;
import java.util.List;

// Instrumento como o catálogo o consome (RF0011).
public record InstrumentoResponse(
        Long id,
        String codigo,
        String nome,
        String descricao,
        String fabricante,
        Short anoFabricacao,
        BigDecimal valorVenda,
        Integer quantidadeEstoque,
        boolean ativo,
        List<String> categorias) {
}
