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

// Item do carrinho (RF0031, RF0032). Índice único (carrinho_id,
// instrumento_id): adicionar o mesmo instrumento de novo soma na
// quantidade da linha existente, não cria uma segunda.
//
// Não há reserva de estoque nesta fase — a RN0031 só compara a quantidade
// pedida com a disponível (contrato de Vendas v3, decisão 1).

@Entity
@Table(name = "item_carrinho")
@Getter
@Setter
@NoArgsConstructor
public class ItemCarrinho {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "carrinho_id", nullable = false)
    private Carrinho carrinho;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "instrumento_id", nullable = false)
    private Instrumento instrumento;

    // CHECK no banco garante > 0.
    @Column(nullable = false)
    private Integer quantidade;

    // Fotografia do valor de venda no momento em que o item entrou no
    // carrinho: se o preço do instrumento mudar depois, o que está no
    // carrinho não muda sozinho debaixo do cliente.
    @Column(name = "valor_unitario", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorUnitario;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ItemCarrinho outro)) {
            return false;
        }
        return id != null && id.equals(outro.getId());
    }

    @Override
    public int hashCode() {
        return ItemCarrinho.class.hashCode();
    }
}
