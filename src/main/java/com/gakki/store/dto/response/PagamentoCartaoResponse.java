package com.gakki.store.dto.response;

import java.math.BigDecimal;

// Linha de pagamento em cartão, como a tela a imprime:
public record PagamentoCartaoResponse(
        String cartaoApelido,
        String ultimosDigitos,
        BigDecimal valor) {
}
