package com.gakki.store.domain.vendas;

import com.gakki.store.auditoria.AuditoriaListener;
import com.gakki.store.domain.cliente.Cliente;
import com.gakki.store.domain.cliente.Endereco;
import com.gakki.store.domain.vendas.enums.StatusPedido;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

// Pedido de compra (RF0038). Raiz do agregado: itens e linhas de pagamento
// são gravados junto, numa única transação.

@Entity
@EntityListeners(AuditoriaListener.class)
@Table(name = "pedido")
@Getter
@Setter
@NoArgsConstructor
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Número único no formato PED-00001, vindo da sequence
    // seq_numero_pedido — mesmo padrão do código do cliente (RNF0035).
    @Column(nullable = false, length = 20, unique = true)
    private String numero;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    // Referência ao endereço cadastrado, não cópia dos campos. Nesta fase
    // basta; quando a entrega entrar em escopo, o endereço precisará ser
    // fotografado para o pedido não mudar se o cliente editar o cadastro.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "endereco_entrega_id", nullable = false)
    private Endereco enderecoEntrega;

    // STRING, nunca ORDINAL: com ORDINAL o banco guardaria 0, 1, 2, e
    // inserir um valor no meio do enum reescreveria o significado de todas
    // as linhas já gravadas.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusPedido status;

    @Column(name = "valor_frete", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorFrete;

    // Itens + frete. Calculado no service a partir do carrinho e do
    // endereço; nunca aceito do front (contrato de Vendas v3, decisão 14).
    @Column(name = "valor_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorTotal;

    @CreationTimestamp
    @Column(name = "criado_em", nullable = false, updatable = false)
    private OffsetDateTime criadoEm;

    // @BatchSize: na listagem "meus pedidos", que é paginada, as duas
    // coleções não podem vir por JOIN FETCH — paginar com fetch de
    // coleção faz o Hibernate paginar em memória. Com o batch, uma
    // página de 20 pedidos carrega os itens de todos em UMA consulta
    // extra, em vez de uma por pedido.
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 20)
    private List<ItemPedido> itens = new ArrayList<>();

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 20)
    private List<Pagamento> pagamentos = new ArrayList<>();

    public void adicionarItem(ItemPedido item) {
        itens.add(item);
        item.setPedido(this);
    }

    public void adicionarPagamento(Pagamento pagamento) {
        pagamentos.add(pagamento);
        pagamento.setPedido(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Pedido outro)) {
            return false;
        }
        return id != null && id.equals(outro.getId());
    }

    @Override
    public int hashCode() {
        return Pedido.class.hashCode();
    }
}
