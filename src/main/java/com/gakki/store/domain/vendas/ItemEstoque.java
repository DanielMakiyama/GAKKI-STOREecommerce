package com.gakki.store.domain.vendas;

import com.gakki.store.auditoria.AuditoriaListener;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
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
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

// Lote de entrada em estoque (RF0051, RN0050). Cada linha é uma entrada
// física, com custo, fornecedor e data próprios — é esse histórico que
// alimenta o recálculo da RN0051, sempre sobre o MAIOR custo registrado.
//
// A saída de estoque (RF0053) não consome lote nesta fase: a baixa está
// fora do escopo desta fatia.

@Entity
@EntityListeners(AuditoriaListener.class)
@Table(name = "item_estoque")
@Getter
@Setter
@NoArgsConstructor
public class ItemEstoque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "instrumento_id", nullable = false)
    private Instrumento instrumento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fornecedor_id", nullable = false)
    private Fornecedor fornecedor;

    // CHECK no banco garante > 0 (RN0061).
    @Column(nullable = false)
    private Integer quantidade;

    // Custo unitário deste lote (RN0062).
    @Column(name = "valor_custo", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorCusto;

    @Column(name = "data_entrada", nullable = false)
    private LocalDate dataEntrada;

    @CreationTimestamp
    @Column(name = "criado_em", nullable = false, updatable = false)
    private OffsetDateTime criadoEm;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ItemEstoque outro)) {
            return false;
        }
        return id != null && id.equals(outro.getId());
    }

    @Override
    public int hashCode() {
        return ItemEstoque.class.hashCode();
    }
}
