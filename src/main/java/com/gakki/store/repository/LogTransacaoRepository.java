package com.gakki.store.repository;

import com.gakki.store.domain.auditoria.LogTransacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

// Acesso ao log de transação (RNF0012).
//
// A consulta por entidade existe porque o RNF0012 pede "manter os dados
// alterados" — o que só faz sentido se for possível consultá-los depois.
// Paginada, como toda listagem do projeto.

public interface LogTransacaoRepository extends JpaRepository<LogTransacao, Long> {

    Page<LogTransacao> findByEntidadeAndEntidadeIdOrderByCriadoEmDesc(
            String entidade, String entidadeId, Pageable paginacao);
}
