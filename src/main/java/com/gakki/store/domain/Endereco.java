package com.gakki.store.domain;

import com.gakki.store.domain.enums.TipoLogradouro;
import com.gakki.store.domain.enums.TipoResidencia;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
 * Endereço de entrega e/ou cobrança do cliente (RN0021, RN0022, RN0023).
 *
 * <p>{@code entrega} e {@code cobranca} são independentes porque o mesmo
 * endereço costuma servir aos dois fins — com um enum de tipo seria
 * preciso duplicar o registro para satisfazer as duas regras.
 */
@Entity
@Table(name = "endereco")
@Getter
@Setter
@NoArgsConstructor
public class Endereco {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    /** Nome curto que identifica o endereço, ex.: Casa, Trabalho (RF0026). */
    @Column(nullable = false, length = 40)
    private String apelido;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_residencia", nullable = false, length = 20)
    private TipoResidencia tipoResidencia;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_logradouro", nullable = false, length = 20)
    private TipoLogradouro tipoLogradouro;

    @Column(nullable = false, length = 150)
    private String logradouro;

    @Column(nullable = false, length = 10)
    private String numero;

    @Column(length = 60)
    private String complemento;

    @Column(nullable = false, length = 80)
    private String bairro;

    /** Somente dígitos. */
    @Column(nullable = false, length = 8)
    private String cep;

    @Column(nullable = false, length = 80)
    private String cidade;

    @Column(nullable = false, length = 2)
    private String estado;

    @Column(nullable = false, length = 60)
    private String pais = "Brasil";

    /** Único campo opcional previsto pela RN0023. */
    @Column(length = 255)
    private String observacoes;

    @Column(nullable = false)
    private boolean entrega = true;

    @Column(nullable = false)
    private boolean cobranca = false;

    /** Endereço de entrega sugerido por padrão no checkout. */
    @Column(nullable = false)
    private boolean principal = false;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Endereco outro)) {
            return false;
        }
        return id != null && id.equals(outro.getId());
    }

    @Override
    public int hashCode() {
        return Endereco.class.hashCode();
    }
}
