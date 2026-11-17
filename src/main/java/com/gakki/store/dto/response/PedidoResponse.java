package com.gakki.store.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

// Pedido (RF0038), nos nomes que a tela "Meus pedidos" já lê.
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
