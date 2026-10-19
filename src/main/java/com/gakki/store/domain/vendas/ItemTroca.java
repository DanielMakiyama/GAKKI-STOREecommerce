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
import lombok.Setter;

/*
 * Item incluído num pedido de troca (RF0041).
 *
 * Aponta para o ItemPedido, e não para o Instrumento: o que se troca é
 * uma linha de uma compra específica, com o preço daquela compra. É esse
 * vínculo que permite calcular o valor do cupom (RF0045) sem consultar o
 * preço atual do catálogo.
 *
 * Não tem @EntityListeners: a troca inteira já gera log de auditoria, e
 * o ExtratorDeEstado pula coleções de propósito — cada item teria log
 * próprio sem acrescentar informação sobre a decisão tomada.
 */
@Entity
@Table(name = "item_troca")
@Getter
@Setter
public class ItemTroca {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "troca_id", nullable = false)
    private Troca troca;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_pedido_id", nullable = false)
    private ItemPedido itemPedido;

    @Column(name = "quantidade", nullable = false)
    private Integer quantidade;

    @Override
    public boolean equals(Object outro) {
        if (this == outro) {
            return true;
        }
        if (!(outro instanceof ItemTroca outroItem)) {
            return false;
        }
        return id != null && id.equals(outroItem.getId());
    }

    @Override
    public int hashCode() {
        return ItemTroca.class.hashCode();
    }
}
