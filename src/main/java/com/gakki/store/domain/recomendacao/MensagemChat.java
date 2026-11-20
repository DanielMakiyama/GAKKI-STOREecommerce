package com.gakki.store.domain.recomendacao;

import com.gakki.store.auditoria.AuditoriaListener;
import com.gakki.store.domain.recomendacao.enums.AutorMensagem;
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
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

// Mensagem de uma conversa. Nas respostas do assistente, modeloIa registra
// qual versão do modelo respondeu (nulo antes do primeiro ajuste fino).
// Sem setters: uma mensagem não muda depois de gravada.

@Entity
@EntityListeners(AuditoriaListener.class)
@Table(name = "mensagem_chat")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MensagemChat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversa_id", nullable = false)
    private ConversaChat conversa;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private AutorMensagem autor;

    @Column(nullable = false, columnDefinition = "text")
    private String texto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modelo_ia_id")
    private ModeloIa modeloIa;

    @CreationTimestamp
    @Column(name = "criado_em", nullable = false, updatable = false)
    private OffsetDateTime criadoEm;

    public static MensagemChat doCliente(ConversaChat conversa, String texto) {
        return nova(conversa, AutorMensagem.CLIENTE, texto, null);
    }

    public static MensagemChat doAssistente(ConversaChat conversa, String texto, ModeloIa modeloIa) {
        return nova(conversa, AutorMensagem.ASSISTENTE, texto, modeloIa);
    }

    private static MensagemChat nova(ConversaChat conversa, AutorMensagem autor, String texto, ModeloIa modeloIa) {
        MensagemChat mensagem = new MensagemChat();
        mensagem.conversa = conversa;
        mensagem.autor = autor;
        mensagem.texto = texto;
        mensagem.modeloIa = modeloIa;
        return mensagem;
    }

    public boolean ehDoAssistente() {
        return autor == AutorMensagem.ASSISTENTE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MensagemChat outra)) {
            return false;
        }
        return id != null && id.equals(outra.getId());
    }

    @Override
    public int hashCode() {
        return MensagemChat.class.hashCode();
    }
}
