package com.gakki.store.dto.response;

import java.math.BigDecimal;

// Cupom como a tela "Meus cupons" o consome (RF0037).
//
// Sem `validoAte`, que o mock devolvia: não existe validade de cupom no
// DRS nem coluna para ela no schema. A tela já trata a ausência — a
// coluna "Válido até" mostra "—". Inventar a data no backend seria criar
// requisito que ninguém pediu.

public record CupomResponse(
        Long id,
        String codigo,
        String tipo,
        BigDecimal valor,
        boolean utilizado) {
}
