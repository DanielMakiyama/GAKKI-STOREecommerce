package com.gakki.store.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Bandeira de cartão aceita pelo sistema (RN0025).
 *
 * <p>Tabela de domínio, populada pela migration V3.
 */
@Entity
@Table(name = "bandeira")
@Getter
@Setter
@NoArgsConstructor
public class Bandeira {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 40, unique = true)
    private String nome;

    /** Bandeira desativada não recebe cartões novos, mas preserva os existentes. */
    @Column(nullable = false)
    private boolean ativo = true;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Bandeira outra)) {
            return false;
        }
        return id != null && id.equals(outra.getId());
    }

    @Override
    public int hashCode() {
        return Bandeira.class.hashCode();
    }
}
