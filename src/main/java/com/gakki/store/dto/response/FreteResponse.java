package com.gakki.store.dto.response;

import java.math.BigDecimal;

// Prévia do frete no checkout (RF0034).
//
// Nomes vindos do mock (`valorFrete`, `valorTotalComFrete`). A diferença
// em relação ao mock é o parâmetro: o nosso cálculo precisa do endereço,
// porque a fórmula cobra adicional fora de SP — o mock calculava só pelo
// peso fictício dos itens.
//
// Este valor é uma PRÉVIA para a tela. O valor que entra no pedido é
// recalculado no finalizarCompra, nunca aceito do front (contrato de
// Vendas v3, decisão 14).

public record FreteResponse(
        BigDecimal valorFrete,
        BigDecimal valorTotalComFrete) {
}
