package com.gakki.store.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

// Uma linha de pagamento em cartão (RF0036, RN0034).

public record PagamentoCartaoRequest(

        @NotNull(message = "Informe o cartão")
        Long cartaoCreditoId,

        @NotNull(message = "Informe o valor")
        @DecimalMin(value = "0.01", message = "O valor deve ser maior que zero")
        BigDecimal valor) {
}
