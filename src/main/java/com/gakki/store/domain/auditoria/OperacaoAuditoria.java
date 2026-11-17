package com.gakki.store.domain.auditoria;

// Operação registrada no log de transação (RNF0012). Os dois valores são
// exatamente os aceitos pelo CHECK ck_log_transacao_operacao.

public enum OperacaoAuditoria {
    INSERCAO,
    ALTERACAO
}
