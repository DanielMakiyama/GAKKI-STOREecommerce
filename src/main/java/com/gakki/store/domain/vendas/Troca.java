package com.gakki.store.domain.vendas;

import com.gakki.store.auditoria.AuditoriaListener;
import com.gakki.store.domain.cliente.Cliente;
import com.gakki.store.domain.vendas.enums.StatusTroca;
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
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/*
 * Pedido de troca (RF0041, RN0041).
 *
 * Agregado raiz do fluxo de troca: tem número próprio, ciclo de vida
 * próprio (RN0041, RN0042) e, ao fim, um cupom próprio (RF0045).
 *
 * `valor` é a soma dos itens trocados, calculada na solicitação e
 * congelada aqui. Precisa ficar gravada e não ser recalculada depois: o
 * cupom emitido semanas mais tarde tem de valer o que a compra valia, e
 * não o que o produto passou a custar.
 */
@Entity
@Table(name = "troca")
@EntityListeners(AuditoriaListener.class)
@Getter
@Setter
public class Troca {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero", nullable = false, length = 20)
    private String numero;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StatusTroca status;

    @Column(name = "valor", nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    // Decisão do administrador na confirmação de recebimento (RF0044).
    // Boolean, e não boolean: nulo significa "ainda não decidido", que é
    // um terceiro estado legítimo enquanto a troca não foi recebida.
    @Column(name = "retorna_estoque")
    private Boolean retornaEstoque;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cupom_id")
    private Cupom cupom;

    // @CreationTimestamp, e não só o DEFAULT now() da coluna: o Hibernate
    // inclui toda coluna mapeada no INSERT, então sem isto ele grava um
    // NULL explícito e o NOT NULL estoura. O default do banco só vale
    // para quem insira por SQL.
    @CreationTimestamp
    @Column(name = "criado_em", nullable = false, updatable = false)
    private OffsetDateTime criadoEm;

    // Sem @UpdateTimestamp de propósito: quem grava aqui é o service, a
    // cada transição de estado da troca, e não toda vez que o Hibernate
    // resolve sincronizar a entidade.
    @Column(name = "atualizado_em")
    private OffsetDateTime atualizadoEm;

    @OneToMany(mappedBy = "troca", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemTroca> itens = new ArrayList<>();

    // Mantém os dois lados da relação em sincronia. Sem isto, acrescentar
    // à lista sem setar a troca no item gravaria null em troca_id.
    public void adicionarItem(ItemTroca item) {
        item.setTroca(this);
        itens.add(item);
    }

    // Apenas pelo id, como em todas as entidades do projeto: incluir as
    // coleções faria duas entidades iguais darem false por causa de um
    // proxy ainda não inicializado.
    @Override
    public boolean equals(Object outro) {
        if (this == outro) {
            return true;
        }
        if (!(outro instanceof Troca outraTroca)) {
            return false;
        }
        return id != null && id.equals(outraTroca.getId());
    }

    @Override
    public int hashCode() {
        return Troca.class.hashCode();
    }
}
