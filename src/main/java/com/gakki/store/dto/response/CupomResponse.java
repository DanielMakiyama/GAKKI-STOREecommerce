package com.gakki.store.dto.response;

import java.math.BigDecimal;

// Cupom como a tela "Meus cupons" o consome (RF0037).
public record CupomResponse(
        Long id,
        String codigo,
        String tipo,
        BigDecimal valor,
        boolean utilizado) {
}
