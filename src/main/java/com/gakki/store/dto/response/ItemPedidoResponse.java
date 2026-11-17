package com.gakki.store.dto.response;

import java.math.BigDecimal;

// Item do pedido como a tela "Meus pedidos" o consome.
public record ItemPedidoResponse(
        Long itemPedidoId,
        Long instrumentoId,
        String instrumento,
        Integer quantidade,
        BigDecimal valorUnitario,
        boolean emTroca) {
}
