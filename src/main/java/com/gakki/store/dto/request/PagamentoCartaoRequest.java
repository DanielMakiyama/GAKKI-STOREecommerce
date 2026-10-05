package com.gakki.store.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

// Uma linha de pagamento em cartão (RF0036, RN0034).
//
// O piso de R$10 NÃO está aqui: a RN0034 é condicional — a RN0035 libera
// valor menor quando há cupom na mesma compra —, e uma anotação de campo
// não enxerga as outras linhas nem os cupons. Fica no PedidoService.
//
// O que fica aqui é só o que é formato: valor positivo.

public record PagamentoCartaoRequest(

        @NotNull(message = "Informe o cartão")
        Long cartaoCreditoId,

        @NotNull(message = "Informe o valor")
        @DecimalMin(value = "0.01", message = "O valor deve ser maior que zero")
        BigDecimal valor) {
}
