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
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/*
 * Item removido do carrinho por expiração do bloqueio (RN0045, RNF0042).
 *
 * Guarda o preço que o item tinha quando entrou no carrinho, e não o
 * atual: a RNF0042 manda apresentar ao cliente o que foi retirado, e
 * mostrar um preço diferente do que ele viu seria desinformação.
 *
 * Não aponta para o Carrinho como relação bidirecional de propósito. Se
 * fosse uma coleção dentro de Carrinho, todo JOIN FETCH de carrinho
 * passaria a ter duas List e esbarraria em MultipleBagFetchException —
 * o mesmo problema que já limita a busca de Pedido.
 */
@Entity
@Table(name = "item_retirado")
@Getter
@Setter
public class ItemRetirado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "carrinho_id", nullable = false)
    private Long carrinhoId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "instrumento_id", nullable = false)
    private Instrumento instrumento;

    @Column(name = "quantidade", nullable = false)
    private Integer quantidade;

    @Column(name = "valor_unitario", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorUnitario;

    // @CreationTimestamp, e não só o DEFAULT now() da coluna: o Hibernate
    // inclui toda coluna mapeada no INSERT, então sem isto ele gravaria um
    // NULL explícito aqui e o NOT NULL estouraria — justamente no momento
    // em que o prazo vence. O default do banco é a segunda linha de
    // defesa, para quem insira por SQL.
    @CreationTimestamp
    @Column(name = "retirado_em", nullable = false, updatable = false)
    private OffsetDateTime retiradoEm;

    @Override
    public boolean equals(Object outro) {
        if (this == outro) {
            return true;
        }
        if (!(outro instanceof ItemRetirado outroItem)) {
            return false;
        }
        return id != null && id.equals(outroItem.getId());
    }

    @Override
    public int hashCode() {
        return ItemRetirado.class.hashCode();
    }
}
