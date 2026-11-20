package com.gakki.store.repository;

import com.gakki.store.domain.recomendacao.FeedbackResposta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FeedbackRespostaRepository extends JpaRepository<FeedbackResposta, Long> {

    Optional<FeedbackResposta> findByMensagemId(Long mensagemId);

    // Avaliações de uma página de mensagens, numa consulta só.
    List<FeedbackResposta> findByMensagemIdIn(Collection<Long> mensagemIds);

    // Todo o feedback, com a resposta e a conversa, para o ajuste fino.
    @Query("""
            select f from FeedbackResposta f
            join fetch f.mensagem m
            join fetch m.conversa
            order by f.id
            """)
    List<FeedbackResposta> todosParaTreinamento();

    // Feedback novo ou alterado depois do corte da versão atual do modelo.
    @Query("""
            select count(f) from FeedbackResposta f
            where f.criadoEm > :desde or f.atualizadoEm > :desde
            """)
    long contarDesde(@Param("desde") OffsetDateTime desde);
}
