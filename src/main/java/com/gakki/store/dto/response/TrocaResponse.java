package com.gakki.store.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/*
 * RF0041 a RF0045 — o pedido de troca como a tela precisa dele.
 *
 * `cupomGerado` só vem preenchido depois da confirmação de recebimento:
 * é o cupom que a RF0045 promete ao cliente. `retornaEstoque` é nulo
 * enquanto o administrador não decidiu.
 */
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
