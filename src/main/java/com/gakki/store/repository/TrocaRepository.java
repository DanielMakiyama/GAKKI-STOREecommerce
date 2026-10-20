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

    // RF0043 — a fila do administrador, com e sem filtro de status.
    //
    // Duas consultas derivadas em vez de uma JPQL com ":status is null":
    // mesma armadilha de inferência de tipo em parâmetro de enum que já
    // apareceu no PedidoRepository.
    Page<Troca> findAllByOrderByCriadoEmDesc(Pageable paginacao);

    Page<Troca> findByStatusOrderByCriadoEmDesc(StatusTroca status, Pageable paginacao);

    /*
     * A troca com os itens, para o administrador agir sobre ela.
     *
     * O JOIN FETCH desce até o item do pedido e o instrumento porque a
     * confirmação de recebimento precisa dos três: do item para saber a
     * quantidade, e do instrumento para a reentrada em estoque (RF0054).
     * Sem isto seriam duas consultas por item dentro do laço.
     *
     * Uma coleção só por consulta — `itens` é a única List aqui, então
     * não há risco de MultipleBagFetchException.
     */
    @Query("""
            select t from Troca t
            left join fetch t.itens item
            left join fetch item.itemPedido itemPedido
            left join fetch itemPedido.instrumento
            where t.id = :id
            """)
    Optional<Troca> buscarComItens(@Param("id") Long id);

    // RF0041 — número do pedido de troca, no formato TRC-00001. Sequence
    // do PostgreSQL, mesmo motivo do número do pedido.
    @Query(value = "SELECT nextval('seq_numero_troca')", nativeQuery = true)
    Long proximoNumeroDeTroca();
}
