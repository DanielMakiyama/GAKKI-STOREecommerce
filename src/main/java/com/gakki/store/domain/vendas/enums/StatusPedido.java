package com.gakki.store.domain.vendas.enums;

// Ciclo de vida do pedido, com os nomes do DRS — não os do protótipo
// mockado (contrato de Vendas v3, observação 3).
//
// Nesta fatia o único valor gravado é EM_PROCESSAMENTO (RF0038): a
// validação de pagamento (RN0037, RN0038), o despacho e a troca estão
// fora de escopo. Os demais já entram no enum para não exigir migration
// nova quando essas fases chegarem — o CHECK da tabela `pedido` já aceita
// todos eles.

public enum StatusPedido {
    EM_PROCESSAMENTO,
    APROVADA,
    REPROVADA,
    EM_TRANSPORTE,
    ENTREGUE,
    EM_TROCA,
    TROCA_AUTORIZADA,
    TROCADO
}
