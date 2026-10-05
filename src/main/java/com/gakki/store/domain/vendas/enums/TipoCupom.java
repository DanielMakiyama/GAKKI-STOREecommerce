package com.gakki.store.domain.vendas.enums;

// Tipo do cupom usado como forma de pagamento (RF0037).
//
// A distinção existe por causa da RN0033: só um cupom PROMOCIONAL por
// compra. Para cupom de TROCA o DRS não documenta limite.

public enum TipoCupom {
    TROCA,
    PROMOCIONAL
}
