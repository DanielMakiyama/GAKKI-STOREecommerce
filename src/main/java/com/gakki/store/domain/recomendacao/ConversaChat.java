package com.gakki.store.domain.recomendacao;

import com.gakki.store.auditoria.AuditoriaListener;
import com.gakki.store.domain.cliente.Cliente;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

// Uma conversa do cliente com o chatbot (RNF0044, item 2).
// As mensagens ficam fora da entidade e são buscadas paginadas pelo repository.

@Entity
@EntityListeners(AuditoriaListener.class)
@Table(name = "conversa_chat")
@Getter
@Setter
@NoArgsConstructor
public class ConversaChat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @CreationTimestamp
    @Column(name = "iniciada_em", nullable = false, updatable = false)
    private OffsetDateTime iniciadaEm;

    public ConversaChat(Cliente cliente) {
        this.cliente = cliente;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ConversaChat outra)) {
            return false;
        }
        return id != null && id.equals(outra.getId());
    }

    @Override
    public int hashCode() {
        return ConversaChat.class.hashCode();
    }
}
