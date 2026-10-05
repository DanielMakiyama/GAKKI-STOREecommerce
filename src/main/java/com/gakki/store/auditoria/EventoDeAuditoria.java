package com.gakki.store.auditoria;

import com.gakki.store.domain.auditoria.OperacaoAuditoria;

import java.util.Map;

// Uma escrita que aconteceu e precisa virar linha de log (RNF0012).
//
// O evento carrega o estado já extraído, não a entidade: quando ele for
// consumido, depois do commit, a sessão do Hibernate pode estar fechada e
// qualquer relação LAZY estouraria LazyInitializationException.

public record EventoDeAuditoria(
        String entidade,
        String entidadeId,
        OperacaoAuditoria operacao,
        String usuarioEmail,
        Map<String, Object> dados) {
}
