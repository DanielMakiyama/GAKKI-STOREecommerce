package com.gakki.store.repository;

import com.gakki.store.domain.recomendacao.ConversaChat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConversaChatRepository extends JpaRepository<ConversaChat, Long> {

    // A conversa em andamento é a mais recente do cliente.
    Optional<ConversaChat> findFirstByClienteIdOrderByIniciadaEmDescIdDesc(Long clienteId);

    // Busca pelo dono: a conversa de outro cliente não é encontrada (404).
    Optional<ConversaChat> findByIdAndClienteId(Long id, Long clienteId);
}
