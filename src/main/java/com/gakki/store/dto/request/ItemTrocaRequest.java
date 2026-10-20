package com.gakki.store.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

// Uma linha da solicitação de troca (RF0041).
public record ItemTrocaRequest(

        @NotNull(message = "Informe o item do pedido.")
        Long itemPedidoId,

        @NotNull(message = "Informe a quantidade a trocar.")
        @Positive(message = "A quantidade a trocar deve ser maior que zero.")
        Integer quantidade) {
}
