package com.gakki.store.domain.vendas.enums;

/*
 * Ciclo de vida do pedido de troca (RN0041, RN0042).
 *
 * Os três nomes são os do DRS, não os do protótipo do front, que
 * trabalhava com seis estados (TROCA_SOLICITADA, ITEM_ENVIADO,
 * ITEM_RECEBIDO e companhia). Vale a mesma decisão tomada no status do
 * pedido: o DRS é a única fonte válida para a especificação, e o front é
 * ajustado para bater com o backend, não o contrário.
 *
 *   EM_TROCA          solicitada pelo cliente (RF0041, RN0041)
 *   TROCA_AUTORIZADA  autorizada pelo administrador (RF0042)
 *   TROCADO           item recebido e cupom emitido (RF0044, RN0042, RF0045)
 *
 * Não existe estado de recusa: a RF0042 só descreve o caminho da
 * autorização, e o DRS não prevê nem estado nem regra para a negativa.
 * A lacuna está registrada na seção 16.2 da Especificação de Caso de Uso
 * e no Plano de Testes; inventar um estado aqui seria criar requisito.
 */
public enum StatusTroca {
    EM_TROCA,
    TROCA_AUTORIZADA,
    TROCADO
}
