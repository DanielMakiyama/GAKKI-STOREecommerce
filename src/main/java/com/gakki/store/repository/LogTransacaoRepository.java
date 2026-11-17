package com.gakki.store.repository;

import com.gakki.store.domain.auditoria.LogTransacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

// Acesso ao log de transação (RNF0012).

public interface LogTransacaoRepository extends JpaRepository<LogTransacao, Long> {

    Page<LogTransacao> findByEntidadeAndEntidadeIdOrderByCriadoEmDesc(
            String entidade, String entidadeId, Pageable paginacao);
}
