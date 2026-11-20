package com.gakki.store.domain.recomendacao;

import com.gakki.store.auditoria.AuditoriaListener;
import com.gakki.store.domain.cliente.Cliente;
import com.gakki.store.domain.recomendacao.enums.AvaliacaoResposta;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

// Avaliação de uma resposta do chatbot pelo cliente (RNF0044, critério d).
// Uma por resposta; avaliar de novo troca a avaliação e marca atualizadoEm.

@Entity
@EntityListeners(AuditoriaListener.class)
@Table(name = "feedback_resposta")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FeedbackResposta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mensagem_id", nullable = false, unique = true)
    private MensagemChat mensagem;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private AvaliacaoResposta avaliacao;

    @CreationTimestamp
    @Column(name = "criado_em", nullable = false, updatable = false)
    private OffsetDateTime criadoEm;

    @Column(name = "atualizado_em")
    private OffsetDateTime atualizadoEm;

    public FeedbackResposta(MensagemChat mensagem, Cliente cliente, AvaliacaoResposta avaliacao) {
        this.mensagem = mensagem;
        this.cliente = cliente;
        this.avaliacao = avaliacao;
    }

    public void reavaliar(AvaliacaoResposta nova) {
        if (nova != avaliacao) {
            this.avaliacao = nova;
            this.atualizadoEm = OffsetDateTime.now();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FeedbackResposta outro)) {
            return false;
        }
        return id != null && id.equals(outro.getId());
    }

    @Override
    public int hashCode() {
        return FeedbackResposta.class.hashCode();
    }
}
