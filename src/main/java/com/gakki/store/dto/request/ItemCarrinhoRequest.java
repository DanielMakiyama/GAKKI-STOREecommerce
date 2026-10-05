package com.gakki.store.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

// Adição de item ao carrinho (RF0031).
//
// Quantidade positiva é formato, validado aqui. "Cabe no estoque" é
// regra de negócio (RN0031) e fica no CarrinhoService: depende do
// instrumento, que esta anotação não enxerga.

public record ItemCarrinhoRequest(

        @NotNull(message = "Informe o instrumento")
        Long instrumentoId,

        @NotNull(message = "Informe a quantidade")
        @Positive(message = "A quantidade deve ser maior que zero")
        Integer quantidade) {
}
