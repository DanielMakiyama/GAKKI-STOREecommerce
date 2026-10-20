package com.gakki.store.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/*
 * O carrinho como a tela precisa dele.
 *
 * Além dos itens, carrega o estado do bloqueio (RN0044) e os itens que
 * saíram por expiração (RNF0042). `podeComprar` é calculado no servidor
 * de propósito: a RNF0042 manda desabilitar a compra enquanto houver
 * item retirado, e essa é uma regra de negócio — deixá-la só no
 * JavaScript da tela a tornaria contornável.
 */
public record CarrinhoResponse(
        Long carrinhoId,
        List<ItemCarrinhoResponse> itens,
        BigDecimal valorTotal,
        OffsetDateTime bloqueadoAte,
        Long prazoBloqueioSegundos,
        List<ItemRetiradoResponse> itensRetirados,
        boolean podeComprar) {

    public static CarrinhoResponse vazio() {
        return new CarrinhoResponse(null, List.of(), BigDecimal.ZERO,
                null, null, List.of(), false);
    }
}
