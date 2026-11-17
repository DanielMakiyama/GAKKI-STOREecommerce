package com.gakki.store.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

// Finalização da compra (RF0038).

public record FinalizarCompraRequest(

        @NotNull(message = "Informe o endereço de entrega")
        Long enderecoEntregaId,

        @Valid
        List<PagamentoCartaoRequest> pagamentosCartao,

        List<String> codigosCupom) {
}
