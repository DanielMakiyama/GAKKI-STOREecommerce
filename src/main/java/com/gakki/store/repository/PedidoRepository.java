package com.gakki.store.repository;

import com.gakki.store.domain.vendas.Pedido;
import com.gakki.store.domain.vendas.enums.StatusPedido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {


    Page<Pedido> findByClienteIdOrderByCriadoEmDesc(Long clienteId, Pageable paginacao);

    @Query("""
            select p from Pedido p
            left join fetch p.itens
            where p.id = :id and p.cliente.id = :clienteId
            """)
    Optional<Pedido> buscarComItens(@Param("id") Long id, @Param("clienteId") Long clienteId);

    // RF0039, RF0040 — o administrador age sobre qualquer pedido, então
    @Query("""
            select p from Pedido p
            left join fetch p.itens
            where p.id = :id
            """)
    Optional<Pedido> buscarComItensParaAdministracao(@Param("id") Long id);

    // RF0039 - a fila do administrador, com e sem filtro de status.
    Page<Pedido> findAllByOrderByCriadoEmDesc(Pageable paginacao);

    Page<Pedido> findByStatusOrderByCriadoEmDesc(StatusPedido status, Pageable paginacao);

    //RN0044 e RN0028 — quanto deste instrumento está comprometido por compras que ainda não foram resolvidas.

    @Query("""
            select coalesce(sum(i.quantidade), 0)
            from ItemPedido i
            where i.instrumento.id = :instrumentoId and i.pedido.status = :status
            """)
    Long quantidadeComStatus(@Param("instrumentoId") Long instrumentoId,
                             @Param("status") StatusPedido status);

    // RF0038 - número único do pedido, no formato PED-00001.
    @Query(value = "SELECT nextval('seq_numero_pedido')", nativeQuery = true)
    Long proximoNumeroDePedido();
}
