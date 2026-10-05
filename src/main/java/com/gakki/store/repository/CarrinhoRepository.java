package com.gakki.store.repository;

import com.gakki.store.domain.vendas.Carrinho;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CarrinhoRepository extends JpaRepository<Carrinho, Long> {

    // UNIQUE(cliente_id) garante que no máximo uma linha volta daqui.
    Optional<Carrinho> findByClienteId(Long clienteId);

    // Carrinho com os itens e o instrumento de cada item, numa consulta só.
    //
    // Sem o JOIN FETCH, mostrar um carrinho de cinco itens custaria 1
    // consulta do carrinho + 1 dos itens + 5 dos instrumentos. É o N+1
    // clássico, e com open-in-view: false ele nem chegaria a acontecer —
    // viraria LazyInitializationException fora da transação.
    //
    // Sem DISTINCT de propósito: o JOIN FETCH de coleção repete a linha do
    // carrinho, mas o Hibernate 6 já elimina a duplicata da entidade raiz
    // sozinho. O DISTINCT só acrescentaria um sort no banco.
    @Query("""
            select c from Carrinho c
            left join fetch c.itens i
            left join fetch i.instrumento
            where c.cliente.id = :clienteId
            """)
    Optional<Carrinho> buscarComItens(@Param("clienteId") Long clienteId);
}
