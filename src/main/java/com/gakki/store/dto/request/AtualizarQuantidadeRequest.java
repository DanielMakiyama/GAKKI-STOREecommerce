package com.gakki.store.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

// Alteração da quantidade de um item já no carrinho (RF0032).
// Zero não é aceito: esvaziar a linha é DELETE, não PUT com quantidade 0.
// O stepper da tela já não deixa baixar de 1, então o front funciona sem

public record AtualizarQuantidadeRequest(

        @NotNull(message = "Informe a quantidade")
        @Positive(message = "A quantidade deve ser maior que zero")
        Integer quantidade) {
}
