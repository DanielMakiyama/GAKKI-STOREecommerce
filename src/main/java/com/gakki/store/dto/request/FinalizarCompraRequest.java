package com.gakki.store.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

// Finalização da compra (RF0038).
//
// Sem frete e sem total: o backend recalcula os dois a partir do carrinho
// e do endereço (contrato de Vendas v3, decisão 14). Aceitar esses
// valores do cliente deixaria o preço da compra sob controle de quem paga.
//
// As duas listas são opcionais e se combinam — a compra pode ser só
// cartão, só cupom ou os dois (RF0037). Que a soma cubra o total é regra
// de negócio, validada no service.
//
// O @Valid na lista é o que faz a validação descer para cada linha de
// cartão; sem ele, só a lista em si seria checada.

public record FinalizarCompraRequest(

        @NotNull(message = "Informe o endereço de entrega")
        Long enderecoEntregaId,

        @Valid
        List<PagamentoCartaoRequest> pagamentosCartao,

        List<String> codigosCupom) {
}
