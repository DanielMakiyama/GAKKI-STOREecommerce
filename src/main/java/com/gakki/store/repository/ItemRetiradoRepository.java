package com.gakki.store.repository;

import com.gakki.store.domain.vendas.ItemRetirado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ItemRetiradoRepository extends JpaRepository<ItemRetirado, Long> {

    // RNF0042 — os itens retirados que a tela do carrinho precisa exibir.
    // O instrumento vem junto porque a listagem mostra o nome dele.
    @Query("""
            select r from ItemRetirado r
            join fetch r.instrumento
            where r.carrinhoId = :carrinhoId
            order by r.retiradoEm desc, r.id desc
            """)
    List<ItemRetirado> doCarrinho(@Param("carrinhoId") Long carrinhoId);

    // O carrinho entra na busca, não num if posterior: um item retirado
    // de outra pessoa simplesmente não é encontrado.
    Optional<ItemRetirado> findByIdAndCarrinhoId(Long id, Long carrinhoId);

    void deleteByCarrinhoId(Long carrinhoId);
}
