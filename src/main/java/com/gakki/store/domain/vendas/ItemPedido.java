package com.gakki.store.domain.vendas;

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

import java.math.BigDecimal;

// Item do pedido (RF0038), fotografado no momento da compra.
//
// `instrumentoNome` e `valorUnitario` são cópias, não leituras do
// instrumento: o pedido é registro histórico. Se o produto for renomeado
// ou reprecificado amanhã, a nota de ontem não pode mudar junto.

@Entity
@Table(name = "item_pedido")
@Getter
@Setter
@NoArgsConstructor
public class ItemPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    // A FK permanece para rastrear qual produto foi vendido; o nome e o
    // valor acima é que não dependem mais dela.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "instrumento_id", nullable = false)
    private Instrumento instrumento;

    @Column(name = "instrumento_nome", nullable = false, length = 150)
    private String instrumentoNome;

    @Column(nullable = false)
    private Integer quantidade;

    @Column(name = "valor_unitario", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorUnitario;

    // Reservado para o processo de troca (RF0041 em diante); sem uso nesta
    // fatia — nenhum código escreve nesta coluna.
    @Column(name = "em_troca", nullable = false)
    private boolean emTroca = false;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ItemPedido outro)) {
            return false;
        }
        return id != null && id.equals(outro.getId());
    }

    @Override
    public int hashCode() {
        return ItemPedido.class.hashCode();
    }
}
