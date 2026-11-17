package com.gakki.store.repository;

import com.gakki.store.domain.vendas.Carrinho;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.Optional;

public interface CarrinhoRepository extends JpaRepository<Carrinho, Long> {

    // UNIQUE(cliente_id) garante que no máximo uma linha volta daqui.
    Optional<Carrinho> findByClienteId(Long clienteId);


    @Query("""
            select c from Carrinho c
            left join fetch c.itens i
            left join fetch i.instrumento
            where c.cliente.id = :clienteId
            """)
    Optional<Carrinho> buscarComItens(@Param("clienteId") Long clienteId);

    @Query("""
            select coalesce(sum(i.quantidade), 0)
            from ItemCarrinho i
            where i.instrumento.id = :instrumentoId
              and i.carrinho.cliente.id <> :clienteId
              and i.carrinho.bloqueadoAte > :agora
            """)
    Long quantidadeBloqueadaPorOutros(@Param("instrumentoId") Long instrumentoId,
                                      @Param("clienteId") Long clienteId,
                                      @Param("agora") OffsetDateTime agora);
}
