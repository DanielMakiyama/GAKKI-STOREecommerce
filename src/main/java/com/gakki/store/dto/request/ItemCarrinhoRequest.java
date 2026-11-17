package com.gakki.store.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

// Adição de item ao carrinho (RF0031).

public record ItemCarrinhoRequest(

        @NotNull(message = "Informe o instrumento")
        Long instrumentoId,

        @NotNull(message = "Informe a quantidade")
        @Positive(message = "A quantidade deve ser maior que zero")
        Integer quantidade) {
}
