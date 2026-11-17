package com.gakki.store.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

// RF0041 a RF0045 — o pedido de troca como a tela precisa dele.
public record TrocaResponse(
        Long id,
        String numero,
        String status,
        String pedidoNumero,
        String clienteNome,
        BigDecimal valor,
        Boolean retornaEstoque,
        CupomResponse cupomGerado,
        OffsetDateTime criadoEm,
        List<ItemTrocaResponse> itens) {
}
