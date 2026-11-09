package com.gakki.store.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

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
