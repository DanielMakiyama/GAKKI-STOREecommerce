package com.gakki.store.domain.vendas;

import com.gakki.store.auditoria.AuditoriaListener;
import com.gakki.store.domain.cliente.Cliente;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

// Carrinho de compra (RF0031, RF0032). Um por cliente, criado sob demanda
// na primeira adição de item — não existe carrinho anônimo nesta fatia
// (contrato de Vendas v3, decisão 11).

@Entity
@EntityListeners(AuditoriaListener.class)
@Table(name = "carrinho")
@Getter
@Setter
@NoArgsConstructor
public class Carrinho {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // UNIQUE(cliente_id) no banco é o que garante o 1:1 de verdade; a
    // anotação sozinha não impede uma segunda linha para o mesmo cliente.
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false, unique = true)
    private Cliente cliente;

    // Mapeada e sem uso nesta fase: o bloqueio temporário de itens (RN0044,
    // RN0045) está fora do escopo do enunciado. Nenhum código escreve aqui.
    @Column(name = "bloqueado_ate")
    private OffsetDateTime bloqueadoAte;

    // O carrinho é a raiz do agregado: item nenhum existe fora dele, e
    // mostrar o carrinho sempre carrega todos os itens. Por isso a coleção
    // existe aqui — diferente de Cliente, que não mapeia endereços nem
    // cartões para não trocar um COUNT por carregar tudo em memória.
    //
    // orphanRemoval: tirar o item da lista apaga a linha. É o que permite
    // esvaziar o carrinho na finalização da compra sem DELETE manual.
    @OneToMany(mappedBy = "carrinho", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemCarrinho> itens = new ArrayList<>();

    // Os dois lados da relação precisam ser ajustados juntos. Acrescentar à
    // lista sem apontar o item de volta para o carrinho grava carrinho_id
    // nulo e estoura o NOT NULL — é o erro clássico de relação bidirecional.
    public void adicionarItem(ItemCarrinho item) {
        itens.add(item);
        item.setCarrinho(this);
    }

    public void removerItem(ItemCarrinho item) {
        itens.remove(item);
        item.setCarrinho(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Carrinho outro)) {
            return false;
        }
        return id != null && id.equals(outro.getId());
    }

    @Override
    public int hashCode() {
        return Carrinho.class.hashCode();
    }
}
