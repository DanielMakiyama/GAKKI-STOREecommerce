package com.gakki.store.repository;

import com.gakki.store.domain.vendas.Pedido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    // "Meus pedidos", do mais recente para o mais antigo. Paginada.
    Page<Pedido> findByClienteIdOrderByCriadoEmDesc(Long clienteId, Pageable paginacao);

    // Detalhe do pedido com os itens. O clienteId faz parte da busca, não
    // de um if posterior: um pedido de outra pessoa simplesmente não é
    // encontrado, e a resposta é 404 em vez de 403 — não confirma sequer
    // que aquele id existe.
    //
    // Só os itens são trazidos por JOIN FETCH, não os pagamentos. Buscar
    // as duas coleções na mesma consulta dispara MultipleBagFetchException:
    // são duas List sem @OrderColumn, e o Hibernate não consegue remontar
    // qual linha do produto cartesiano pertence a qual coleção. Os
    // pagamentos carregam LAZY dentro do método @Transactional do service —
    // uma consulta a mais, não N.
    @Query("""
            select p from Pedido p
            left join fetch p.itens
            where p.id = :id and p.cliente.id = :clienteId
            """)
    Optional<Pedido> buscarComItens(@Param("id") Long id, @Param("clienteId") Long clienteId);

    // RF0038 — número único do pedido, no formato PED-00001.
    //
    // Sequence do PostgreSQL, mesmo motivo do código do cliente (RNF0035):
    // um contador em memória reiniciaria com a aplicação e colidiria entre
    // duas instâncias.
    @Query(value = "SELECT nextval('seq_numero_pedido')", nativeQuery = true)
    Long proximoNumeroDePedido();
}
