package com.gakki.store.dto.response;

import java.math.BigDecimal;

// Item do carrinho como a tela o consome.
//
// Os nomes espelham o mock do front (`itemId`, `nomeInstrumento`), não os
// das entidades. A tela Carrinho.jsx já lê exatamente esses campos — sair
// dos nomes dela obrigaria a mexer no componente sem necessidade.
//
// Sem `lockExpiraEm`: o mock o devolvia, mas nenhuma tela o usa e o
// bloqueio temporário (RN0044) está fora do escopo desta fase.

public record ItemCarrinhoResponse(
        Long itemId,
        Long instrumentoId,
        String nomeInstrumento,
        Integer quantidade,
        BigDecimal valorUnitario) {
}
