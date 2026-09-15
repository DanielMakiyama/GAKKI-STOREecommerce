package com.gakki.store.domain;

import com.gakki.store.domain.enums.Genero;
import com.gakki.store.domain.enums.TipoTelefone;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * Dados cadastrais do cliente (RN0026).
 *
 * <p>Não tem coleções de endereços e cartões de propósito. Verificar a
 * RN0022 com {@code enderecoRepository.countByClienteIdAndEntregaTrue}
 * custa um COUNT; com uma coleção mapeada, custaria carregar todos os
 * endereços do cliente para contar em memória.
 */
@Entity
@Table(name = "cliente")
@Getter
@Setter
@NoArgsConstructor
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    /** Código único no formato CLI-0001 (RNF0035). */
    @Column(nullable = false, length = 20, unique = true)
    private String codigo;

    @Column(nullable = false, length = 150)
    private String nome;

    /** Somente dígitos — a máscara é responsabilidade da tela. */
    @Column(nullable = false, length = 11, unique = true)
    private String cpf;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Genero genero;

    @Column(name = "data_nascimento", nullable = false)
    private LocalDate dataNascimento;

    @Enumerated(EnumType.STRING)
    @Column(name = "telefone_tipo", nullable = false, length = 12)
    private TipoTelefone telefoneTipo;

    @Column(name = "telefone_ddd", nullable = false, length = 2)
    private String telefoneDdd;

    @Column(name = "telefone_numero", nullable = false, length = 9)
    private String telefoneNumero;

    @CreationTimestamp
    @Column(name = "criado_em", nullable = false, updatable = false)
    private OffsetDateTime criadoEm;

    @UpdateTimestamp
    @Column(name = "atualizado_em")
    private OffsetDateTime atualizadoEm;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Cliente outro)) {
            return false;
        }
        return id != null && id.equals(outro.getId());
    }

    @Override
    public int hashCode() {
        return Cliente.class.hashCode();
    }
}
