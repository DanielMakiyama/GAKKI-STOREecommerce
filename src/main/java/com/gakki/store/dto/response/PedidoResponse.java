package com.gakki.store.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

// Pedido (RF0038), nos nomes que a tela "Meus pedidos" já lê.
//
// `cuponsUtilizados` é uma lista de CÓDIGOS, não de objetos: a tela faz
// `p.cuponsUtilizados.join(", ")`, e devolver objetos imprimiria
// "[object Object]". O contrato de Vendas v3 previa objetos — a tela
// venceu, e o contrato é que será corrigido.
//
// `cupomTrocoGerado` é acréscimo nosso, nulo quando não houve sobra
// (RN0036). A tela atual ignora campo que não conhece; a prova do troco
// na interface é a tela "Meus cupons", onde o cupom novo aparece.

public record PedidoResponse(
        Long id,
        String numero,
        String status,
        String clienteNome,
        String clienteEmail,
        String enderecoResumo,
        BigDecimal valorFrete,
        BigDecimal valorTotal,
        OffsetDateTime criadoEm,
        List<ItemPedidoResponse> itens,
        List<PagamentoCartaoResponse> pagamentosCartao,
        List<String> cuponsUtilizados,
        CupomResponse cupomTrocoGerado) {
}
