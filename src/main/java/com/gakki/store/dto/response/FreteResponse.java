package com.gakki.store.dto.response;

import java.math.BigDecimal;

// Prévia do frete no checkout (RF0034).
public record FreteResponse(
        BigDecimal valorFrete,
        BigDecimal valorTotalComFrete) {
}
