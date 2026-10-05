package com.gakki.store.dto.response;

import java.math.BigDecimal;

// Linha de pagamento em cartão, como a tela a imprime:
// "Pago com: Cartão principal •••• 4321 (R$ 30,00)".
//
// Sem id de cartão e sem nome do titular: a tela não usa, e dado de
// cartão que não precisa sair, não sai.

public record PagamentoCartaoResponse(
        String cartaoApelido,
        String ultimosDigitos,
        BigDecimal valor) {
}
