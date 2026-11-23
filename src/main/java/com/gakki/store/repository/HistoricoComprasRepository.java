package com.gakki.store.repository;

import com.gakki.store.domain.vendas.Pedido;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;

// Vendas lidas pelo chatbot (RNF0044): o histórico de um cliente vai como
// contexto da conversa, e o de todos os clientes vira exemplo de ajuste fino.
// Só leitura, como o AnaliseVendasRepository.

public interface HistoricoComprasRepository extends Repository<Pedido, Long> {

    String SELECT_ITENS = """
            SELECT p.cliente_id                                                AS "clienteId",
                   p.numero                                                    AS "pedido",
                   to_char(p.criado_em AT TIME ZONE :fuso, 'YYYY-MM-DD')       AS "data",
                   i.codigo                                                    AS "codigo",
                   ip.instrumento_nome                                         AS "nome",
                   (SELECT string_agg(c.nome, ', ' ORDER BY c.nome)
                      FROM instrumento_categoria ic
                      JOIN categoria c ON c.id = ic.categoria_id
                     WHERE ic.instrumento_id = ip.instrumento_id)              AS "categorias",
                   ip.quantidade                                               AS "quantidade",
                   ip.valor_unitario                                           AS "valorUnitario"
              FROM pedido p
              JOIN item_pedido ip ON ip.pedido_id = p.id
              JOIN instrumento i  ON i.id = ip.instrumento_id
            """;

    @Query(value = SELECT_ITENS + """
             WHERE p.status IN (:status)
               AND p.cliente_id = :clienteId
             ORDER BY p.criado_em, p.id, ip.id
            """, nativeQuery = true)
    List<ItemComprado> itensDoCliente(@Param("clienteId") Long clienteId,
                                      @Param("status") Collection<String> status,
                                      @Param("fuso") String fuso);

    @Query(value = SELECT_ITENS + """
             WHERE p.status IN (:status)
             ORDER BY p.cliente_id, p.criado_em, p.id, ip.id
            """, nativeQuery = true)
    List<ItemComprado> itensDeTodosOsClientes(@Param("status") Collection<String> status,
                                              @Param("fuso") String fuso);

    // Ajuste fino: todas as vendas até o corte (dados_ate) da versão do modelo.
    @Query(value = SELECT_ITENS + """
             WHERE p.status IN (:status)
               AND p.criado_em <= :ate
             ORDER BY p.cliente_id, p.criado_em, p.id, ip.id
            """, nativeQuery = true)
    List<ItemComprado> itensDeTodosOsClientesAte(@Param("status") Collection<String> status,
                                                 @Param("fuso") String fuso,
                                                 @Param("ate") OffsetDateTime ate);

    // Vendas posteriores ao corte da versão atual do modelo.
    @Query(value = """
            SELECT count(*) FROM pedido
             WHERE status IN (:status) AND criado_em > :desde
            """, nativeQuery = true)
    long contarPedidosDesde(@Param("status") Collection<String> status,
                            @Param("desde") OffsetDateTime desde);

    // Aliases entre aspas: sem elas o PostgreSQL rebaixa para minúsculas e a
    // projeção não encontra "clienteId" nem "valorUnitario".
    interface ItemComprado {

        Long getClienteId();

        String getPedido();

        String getData();

        String getCodigo();

        String getNome();

        String getCategorias();

        Integer getQuantidade();

        BigDecimal getValorUnitario();
    }
}
