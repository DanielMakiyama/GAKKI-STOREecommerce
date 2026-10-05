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

import java.math.BigDecimal;

// Grupo de margem de lucro aplicado ao valor de venda (RN0013, RF0052).
// A margem é BigDecimal, nunca double: valor monetário não aceita o erro
// de arredondamento do ponto flutuante binário.

@Entity
@Table(name = "grupo_precificacao")
@Getter
@Setter
@NoArgsConstructor
public class GrupoPrecificacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 60, unique = true)
    private String nome;

    // Percentual sobre o maior custo registrado do instrumento (RN0051).
    // Ex.: 40.00 para 40%.
    @Column(name = "margem_percentual", nullable = false, precision = 5, scale = 2)
    private BigDecimal margemPercentual;

    @Column(nullable = false)
    private boolean ativo = true;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof GrupoPrecificacao outro)) {
            return false;
        }
        return id != null && id.equals(outro.getId());
    }

    @Override
    public int hashCode() {
        return GrupoPrecificacao.class.hashCode();
    }
}
