package com.gakki.store.auditoria;

import com.gakki.store.domain.auditoria.OperacaoAuditoria;

import java.util.Map;

// Uma escrita que aconteceu e precisa virar linha de log (RNF0012).

public record EventoDeAuditoria(
        String entidade,
        String entidadeId,
        OperacaoAuditoria operacao,
        String usuarioEmail,
        Map<String, Object> dados) {
}
