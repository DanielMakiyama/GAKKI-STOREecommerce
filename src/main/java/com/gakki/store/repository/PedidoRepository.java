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

    // RF0039, RF0040 — o administrador age sobre qualquer pedido, então
    // aqui o dono NÃO entra na chave de acesso. A proteção muda de lugar:
    // passa a ser o @PreAuthorize("hasRole('ADMIN')") no controller.
    //
    // Mesma restrição de uma coleção por JOIN FETCH da consulta acima.
    @Query("""
            select p from Pedido p
            left join fetch p.itens
            where p.id = :id
            """)
    Optional<Pedido> buscarComItensParaAdministracao(@Param("id") Long id);

    // RF0039 — a fila do administrador, com e sem filtro de status.
    //
    // São duas consultas derivadas em vez de uma JPQL com
    // "(:status is null or p.status = :status)": passar null num
    // parâmetro de enum é justamente o caso em que o Hibernate 6 não
    // consegue inferir o tipo, e o service escolhe a consulta em uma
    // linha. A ordenação vem no nome do método porque a tela precisa do
    // mais recente primeiro mesmo quando o Pageable chega sem sort.
    Page<Pedido> findAllByOrderByCriadoEmDesc(Pageable paginacao);

    Page<Pedido> findByStatusOrderByCriadoEmDesc(StatusPedido status, Pageable paginacao);

    /*
     * RN0044 e RN0028 — quanto deste instrumento está comprometido por
     * compras que ainda não foram resolvidas.
     *
     * Entre finalizar e a operadora responder, a mercadoria continua
     * bloqueada: é o que a RN0044 quer dizer com "o bloqueio só deve ser
     * retirado no caso da compra não ser efetivada ou aprovada". Aprovada,
     * vira baixa; reprovada, o pedido sai de EM_PROCESSAMENTO e o
     * bloqueio desaparece sozinho, sem ninguém precisar "desbloquear".
     */
    @Query("""
            select coalesce(sum(i.quantidade), 0)
            from ItemPedido i
            where i.instrumento.id = :instrumentoId and i.pedido.status = :status
            """)
    Long quantidadeComStatus(@Param("instrumentoId") Long instrumentoId,
                             @Param("status") StatusPedido status);

    // RF0038 — número único do pedido, no formato PED-00001.
    //
    // Sequence do PostgreSQL, mesmo motivo do código do cliente (RNF0035):
    // um contador em memória reiniciaria com a aplicação e colidiria entre
    // duas instâncias.
    @Query(value = "SELECT nextval('seq_numero_pedido')", nativeQuery = true)
    Long proximoNumeroDePedido();
}
