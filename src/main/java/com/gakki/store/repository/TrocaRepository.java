package com.gakki.store.repository;

import com.gakki.store.domain.vendas.Troca;
import com.gakki.store.domain.vendas.enums.StatusTroca;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TrocaRepository extends JpaRepository<Troca, Long> {

    // "Minhas trocas", do mais recente para o mais antigo.
    Page<Troca> findByClienteIdOrderByCriadoEmDesc(Long clienteId, Pageable paginacao);

    // RF0043 - a fila do administrador, com e sem filtro de status.
    Page<Troca> findAllByOrderByCriadoEmDesc(Pageable paginacao);

    Page<Troca> findByStatusOrderByCriadoEmDesc(StatusTroca status, Pageable paginacao);

    @Query("""
            select t from Troca t
            left join fetch t.itens item
            left join fetch item.itemPedido itemPedido
            left join fetch itemPedido.instrumento
            where t.id = :id
            """)
    Optional<Troca> buscarComItens(@Param("id") Long id);

    // RF0041 - número do pedido de troca, no formato TRC-00001.
    @Query(value = "SELECT nextval('seq_numero_troca')", nativeQuery = true)
    Long proximoNumeroDeTroca();
}
