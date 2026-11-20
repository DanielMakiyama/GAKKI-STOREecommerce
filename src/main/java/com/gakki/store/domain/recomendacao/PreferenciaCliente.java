package com.gakki.store.domain.recomendacao;

import com.gakki.store.auditoria.AuditoriaListener;
import com.gakki.store.domain.cliente.Cliente;
import com.gakki.store.domain.recomendacao.enums.EstiloMusical;
import com.gakki.store.domain.recomendacao.enums.NivelMusical;
import com.gakki.store.domain.vendas.Categoria;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

// Preferências musicais informadas pelo cliente no perfil (RNF0044, critério e).
// Uma por cliente; a IA usa junto com o histórico de compras.

@Entity
@EntityListeners(AuditoriaListener.class)
@Table(name = "preferencia_cliente")
@Getter
@Setter
@NoArgsConstructor
public class PreferenciaCliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false, unique = true)
    private Cliente cliente;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NivelMusical nivel;

    // Opcional: nulo significa que o cliente não definiu um teto.
    @Column(name = "orcamento_maximo", precision = 12, scale = 2)
    private BigDecimal orcamentoMaximo;

    @Column(length = 300)
    private String observacoes;

    // Set, e não List: duas coleções buscadas juntas por @EntityGraph
    // precisam ser Set, senão o Hibernate recusa (MultipleBagFetchException).
    @ManyToMany
    @JoinTable(name = "preferencia_cliente_categoria",
            joinColumns = @JoinColumn(name = "preferencia_id"),
            inverseJoinColumns = @JoinColumn(name = "categoria_id"))
    private Set<Categoria> categorias = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "preferencia_cliente_estilo",
            joinColumns = @JoinColumn(name = "preferencia_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "estilo", nullable = false, length = 20)
    private Set<EstiloMusical> estilos = new HashSet<>();

    @CreationTimestamp
    @Column(name = "criado_em", nullable = false, updatable = false)
    private OffsetDateTime criadoEm;

    @UpdateTimestamp
    @Column(name = "atualizado_em")
    private OffsetDateTime atualizadoEm;

    public void substituirCategorias(Set<Categoria> novas) {
        categorias.clear();
        categorias.addAll(novas);
    }

    public void substituirEstilos(Set<EstiloMusical> novos) {
        estilos.clear();
        estilos.addAll(novos);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PreferenciaCliente outra)) {
            return false;
        }
        return id != null && id.equals(outra.getId());
    }

    @Override
    public int hashCode() {
        return PreferenciaCliente.class.hashCode();
    }
}
