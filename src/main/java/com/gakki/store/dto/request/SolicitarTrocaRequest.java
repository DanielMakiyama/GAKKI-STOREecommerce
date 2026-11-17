package com.gakki.store.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

// RF0041 - solicitação de troca de itens de uma compra.

public record SolicitarTrocaRequest(

        @NotNull(message = "Informe o pedido cujos itens serão trocados.")
        Long pedidoId,

        @NotEmpty(message = "Selecione ao menos um item para troca.")
        @Valid
        List<ItemTrocaRequest> itens) {
}
