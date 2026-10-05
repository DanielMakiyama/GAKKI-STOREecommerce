package com.gakki.store.domain.vendas;

import com.gakki.store.auditoria.AuditoriaListener;
import com.gakki.store.domain.cliente.Cliente;
import com.gakki.store.domain.vendas.enums.TipoCupom;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
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
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

// Cupom de troca ou promocional, usável como forma de pagamento (RF0037).
//
// Pertence sempre a um cliente, inclusive o promocional — simplifica a
// validação de posse e basta para esta fase (contrato de Vendas v3,
// decisão 15). Num sistema real o promocional seria global.

@Entity
@EntityListeners(AuditoriaListener.class)
@Table(name = "cupom")
@Getter
@Setter
@NoArgsConstructor
public class Cupom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20, unique = true)
    private String codigo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoCupom tipo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    // Cupom consumido não pode ser reaplicado. A flag vira true na mesma
    // transação que grava o pedido (RF0038).
    @Column(nullable = false)
    private boolean utilizado = false;

    // Nulo enquanto não utilizado — por isso optional = true aqui, ao
    // contrário de todas as outras relações do módulo.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_utilizacao_id")
    private Pedido pedidoUtilizacao;

    @CreationTimestamp
    @Column(name = "criado_em", nullable = false, updatable = false)
    private OffsetDateTime criadoEm;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Cupom outro)) {
            return false;
        }
        return id != null && id.equals(outro.getId());
    }

    @Override
    public int hashCode() {
        return Cupom.class.hashCode();
    }
}
