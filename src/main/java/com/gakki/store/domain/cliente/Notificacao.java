package com.gakki.store.domain.cliente;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

/*
 * Notificação ao cliente (RN0046).
 *
 * Mora em domain/cliente porque pertence ao cliente, não à venda: a
 * RN0046 é o primeiro emissor, mas nada no modelo amarra a notificação
 * ao fluxo de troca.
 *
 * Deliberadamente mínima. A regra manda "gerar uma notificação", sem
 * dizer canal, formato ou leitura — um registro consultável atende o
 * requisito, e inventar e-mail ou push seria criar requisito que o DRS
 * não tem.
 */
@Entity
@Table(name = "notificacao")
@Getter
@Setter
public class Notificacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Column(name = "mensagem", nullable = false, length = 255)
    private String mensagem;

    @Column(name = "lida", nullable = false)
    private boolean lida;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private OffsetDateTime criadoEm;

    @Override
    public boolean equals(Object outro) {
        if (this == outro) {
            return true;
        }
        if (!(outro instanceof Notificacao outraNotificacao)) {
            return false;
        }
        return id != null && id.equals(outraNotificacao.getId());
    }

    @Override
    public int hashCode() {
        return Notificacao.class.hashCode();
    }
}
