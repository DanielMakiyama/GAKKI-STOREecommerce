package com.gakki.store.domain;

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
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Cartão de crédito do cliente (RN0024, RN0025, RF0027).
 *
 * <p>Divergência consciente da RN0024: número completo e código de
 * segurança não são armazenados — exigiriam certificação PCI-DSS. Num
 * fluxo real esses dados vão direto ao gateway, que devolve um token.
 */
@Entity
@Table(name = "cartao")
@Getter
@Setter
@NoArgsConstructor
public class Cartao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bandeira_id", nullable = false)
    private Bandeira bandeira;

    @Column(nullable = false, length = 40)
    private String apelido;

    /** Apenas os quatro últimos dígitos. */
    @Column(name = "ultimos_digitos", nullable = false, length = 4)
    private String ultimosDigitos;

    @Column(name = "nome_titular", nullable = false, length = 100)
    private String nomeTitular;

    @Column(name = "validade_mes", nullable = false)
    private Short validadeMes;

    @Column(name = "validade_ano", nullable = false)
    private Short validadeAno;

    /** Cartão sugerido por padrão no checkout (RF0027). */
    @Column(nullable = false)
    private boolean preferencial = false;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Cartao outro)) {
            return false;
        }
        return id != null && id.equals(outro.getId());
    }

    @Override
    public int hashCode() {
        return Cartao.class.hashCode();
    }
}
