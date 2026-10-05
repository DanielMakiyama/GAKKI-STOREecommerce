package com.gakki.store.dto.response;

import java.math.BigDecimal;
import java.util.List;

// Carrinho do cliente (RF0031, RF0032).
//
// `valorTotal` é só a soma dos itens, sem frete: a tela do carrinho
// mostra "Frete: calculado na próxima etapa", porque o frete depende do
// endereço, que só é escolhido no checkout (RF0034).

public record CarrinhoResponse(
        Long carrinhoId,
        List<ItemCarrinhoResponse> itens,
        BigDecimal valorTotal) {

    // Cliente que nunca adicionou nada não tem linha de carrinho no banco
    // — o carrinho é criado sob demanda. A tela, porém, precisa de uma
    // resposta 200 com lista vazia, e não de um 404.
    public static CarrinhoResponse vazio() {
        return new CarrinhoResponse(null, List.of(), BigDecimal.ZERO);
    }
}
