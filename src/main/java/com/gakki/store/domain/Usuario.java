package com.gakki.store.domain;

import com.gakki.store.domain.enums.Papel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

/**
 * Credencial de acesso ao sistema.
 *
 * <p>Separada de {@link Cliente} porque administrador e gerente de vendas
 * autenticam sem ter dados cadastrais de cliente.
 */
@Entity
@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150, unique = true)
    private String email;

    /** Hash BCrypt — nunca a senha em texto claro (RNF0033). */
    @Column(nullable = false, length = 60)
    private String senha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Papel papel;

    /** FALSE = cadastro inativado. O registro nunca é excluído (RF0023). */
    @Column(nullable = false)
    private boolean ativo = true;

    @CreationTimestamp
    @Column(name = "criado_em", nullable = false, updatable = false)
    private OffsetDateTime criadoEm;

    @UpdateTimestamp
    @Column(name = "atualizado_em")
    private OffsetDateTime atualizadoEm;

    public Usuario(String email, String senha, Papel papel) {
        this.email = email;
        this.senha = senha;
        this.papel = papel;
    }

    // equals/hashCode apenas pelo ID.
    //
    // O hashCode é constante por classe de propósito: uma entidade nova
    // entra numa coleção com id nulo e recebe o id só no flush. Se o hash
    // dependesse do id, ele mudaria depois da inserção e a entidade se
    // perderia dentro do próprio HashSet em que foi colocada.

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Usuario outro)) {
            return false;
        }
        return id != null && id.equals(outro.getId());
    }

    @Override
    public int hashCode() {
        return Usuario.class.hashCode();
    }
}
