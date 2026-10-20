package com.gakki.store.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

// RNF0042 — item que saiu do carrinho por expiração do bloqueio.
public record ItemRetiradoResponse(
        Long itemRetiradoId,
        Long instrumentoId,
        String nomeInstrumento,
        Integer quantidade,
        BigDecimal valorUnitario,
        OffsetDateTime retiradoEm) {
}
