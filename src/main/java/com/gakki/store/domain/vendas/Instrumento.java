package com.gakki.store.domain.vendas;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

// Catálogo de instrumentos (RF0011). Equivale ao "livro" do DRS.
//
// `quantidadeEstoque` é somente lida pelo fluxo de criação de pedido: a
// RN0031 compara a quantidade pedida com a disponível, e nada decrementa
// nesta fase (contrato de Vendas v3, decisão 1).

@Entity
@Table(name = "instrumento")
@Getter
@Setter
@NoArgsConstructor
public class Instrumento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Código único no formato INST-0001, vindo da sequence
    // seq_codigo_instrumento (RNF0021).
    @Column(nullable = false, length = 20, unique = true)
    private String codigo;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, length = 500)
    private String descricao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fabricante_id", nullable = false)
    private Fabricante fabricante;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "grupo_precificacao_id", nullable = false)
    private GrupoPrecificacao grupoPrecificacao;

    @Column(name = "ano_fabricacao", nullable = false)
    private Short anoFabricacao;

    // Recalculado a cada entrada em estoque, sobre o maior custo já
    // registrado entre os lotes (RN0051).
    @Column(name = "valor_venda", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorVenda = BigDecimal.ZERO;

    @Column(name = "quantidade_estoque", nullable = false)
    private Integer quantidadeEstoque = 0;

    @Column(nullable = false)
    private boolean ativo = true;

    @CreationTimestamp
    @Column(name = "criado_em", nullable = false, updatable = false)
    private OffsetDateTime criadoEm;

    // N:N sem entidade associativa própria: o vínculo não carrega atributo
    // nenhum além das duas chaves (RN0012). LAZY por padrão em @ManyToMany;
    // as consultas que precisam das categorias usam @EntityGraph.
    @ManyToMany
    @JoinTable(
            name = "instrumento_categoria",
            joinColumns = @JoinColumn(name = "instrumento_id"),
            inverseJoinColumns = @JoinColumn(name = "categoria_id"))
    private Set<Categoria> categorias = new LinkedHashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Instrumento outro)) {
            return false;
        }
        return id != null && id.equals(outro.getId());
    }

    @Override
    public int hashCode() {
        return Instrumento.class.hashCode();
    }
}
