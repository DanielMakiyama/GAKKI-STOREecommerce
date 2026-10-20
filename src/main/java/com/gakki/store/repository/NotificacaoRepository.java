package com.gakki.store.repository;

import com.gakki.store.domain.cliente.Notificacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {

    // RN0046 — as notificações do cliente, da mais recente para a mais
    // antiga. O dono entra na consulta, não num if posterior.
    Page<Notificacao> findByClienteIdOrderByCriadoEmDesc(Long clienteId, Pageable paginacao);
}
