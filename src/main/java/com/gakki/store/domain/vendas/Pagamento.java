package com.gakki.store.domain.vendas;

import com.gakki.store.domain.cliente.Cartao;
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

// Uma linha de pagamento de um pedido: cartão (RF0036) OU cupom (RF0037),
// nunca os dois. O CHECK ck_pagamento_um_tipo garante isso no banco.
//
// Linha genérica em vez de duas tabelas porque uma compra pode somar
// vários cartões e vários cupons (RN0034, RN0035) e o PedidoResponse monta
// as duas listas a partir daqui (contrato de Vendas v3, decisão 6).
//
// O piso de R$10 por cartão NÃO está no banco: a RN0034 é condicional —
// a RN0035 libera valor menor quando há cupom no mesmo pedido —, e isso
// depende das outras linhas, o que um CHECK de tabela não enxerga. Fica no
// PedidoService.

@Entity
@Table(name = "pagamento")
@Getter
@Setter
@NoArgsConstructor
public class Pagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    // Os dois são opcionais em Java porque exatamente um deles é
    // preenchido por linha.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cartao_id")
    private Cartao cartao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cupom_id")
    private Cupom cupom;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Pagamento outro)) {
            return false;
        }
        return id != null && id.equals(outro.getId());
    }

    @Override
    public int hashCode() {
        return Pagamento.class.hashCode();
    }
}
