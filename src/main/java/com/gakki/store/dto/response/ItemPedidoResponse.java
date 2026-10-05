package com.gakki.store.dto.response;

import java.math.BigDecimal;

// Item do pedido como a tela "Meus pedidos" o consome.
//
// Atenção ao nome: aqui o campo do nome do produto é `instrumento`, e no
// carrinho é `nomeInstrumento`. A diferença não é minha — é como o mock
// do front já estava, e cada tela lê o seu. Uniformizar obrigaria a mexer
// nas duas telas sem ganho nenhum.

public record ItemPedidoResponse(
        Long itemPedidoId,
        Long instrumentoId,
        String instrumento,
        Integer quantidade,
        BigDecimal valorUnitario,
        boolean emTroca) {
}
