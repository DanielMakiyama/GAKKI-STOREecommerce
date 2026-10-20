package com.gakki.store.dto.response;

import java.math.BigDecimal;

public record ItemTrocaResponse(
        Long itemTrocaId,
        Long itemPedidoId,
        String instrumento,
        Integer quantidade,
        BigDecimal valorUnitario) {
}
