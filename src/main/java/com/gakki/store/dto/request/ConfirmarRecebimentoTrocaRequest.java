package com.gakki.store.dto.request;

import jakarta.validation.constraints.NotNull;

// RF0044 - na confirmação de recebimento, o administrador informa se os itens trocados voltam ao estoque.

public record ConfirmarRecebimentoTrocaRequest(

        @NotNull(message = "Informe se os itens retornam ao estoque.")
        Boolean retornaEstoque) {
}
