package com.gakki.store.repository;

import com.gakki.store.domain.recomendacao.MensagemChat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MensagemChatRepository extends JpaRepository<MensagemChat, Long> {

    // Histórico exibido no chat, das mais recentes para as mais antigas.
    Page<MensagemChat> findByConversaIdOrderByCriadoEmDescIdDesc(Long conversaId, Pageable paginacao);

    // Últimas mensagens da conversa, enviadas à IA para ela manter o assunto.
    List<MensagemChat> findTop12ByConversaIdOrderByCriadoEmDescIdDesc(Long conversaId);

    // Só o dono da conversa pode avaliar a resposta.
    Optional<MensagemChat> findByIdAndConversaClienteId(Long id, Long clienteId);

    // Conversas inteiras, em ordem, para montar os exemplos do ajuste fino.
    List<MensagemChat> findByConversaIdInOrderByConversaIdAscCriadoEmAscIdAsc(Collection<Long> conversaIds);
}
