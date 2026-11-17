package com.gakki.store.dto.response;

import java.math.BigDecimal;

// Item do carrinho como a tela o consome.
public record ItemCarrinhoResponse(
        Long itemId,
        Long instrumentoId,
        String nomeInstrumento,
        Integer quantidade,
        BigDecimal valorUnitario) {
}
