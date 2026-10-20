package com.gakki.store.dto.request;

import jakarta.validation.constraints.NotNull;

/*
 * RF0044 — na confirmação de recebimento, o administrador informa se os
 * itens trocados voltam ao estoque.
 *
 * Boolean com @NotNull, e não boolean: a RF0044 exige a decisão
 * explícita, e um primitivo faria "não informado" virar "não retorna"
 * silenciosamente.
 */
public record ConfirmarRecebimentoTrocaRequest(

        @NotNull(message = "Informe se os itens retornam ao estoque.")
        Boolean retornaEstoque) {
}
