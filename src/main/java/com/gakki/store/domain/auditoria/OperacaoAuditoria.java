package com.gakki.store.domain.auditoria;

// Operação registrada no log de transação (RNF0012). Os dois valores são
// exatamente os aceitos pelo CHECK ck_log_transacao_operacao.
//
// Só inserção e alteração: o RNF0012 fala em "operação de escrita
// (Inserção ou Alteração)" — exclusão não aparece, e o sistema não exclui
// cadastro de cliente de qualquer forma (RF0023, inativação lógica).

public enum OperacaoAuditoria {
    INSERCAO,
    ALTERACAO
}
