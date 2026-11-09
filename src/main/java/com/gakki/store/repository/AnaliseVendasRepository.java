package com.gakki.store.repository;

import com.gakki.store.domain.vendas.Pedido;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;

/*
 * Histórico de vendas por categoria (RF0055, RNF0043, RNF0011).
 *
 * Estende `Repository`, e não `JpaRepository`, de propósito: esta
 * interface só lê. Com `JpaRepository` ela herdaria save e delete de
 * pedido, que nada aqui deveria oferecer — o pedido já tem o seu próprio
 * repositório para isso.
 *
 * Uma consulta só, agregada no banco. Trazer os pedidos para somar em
 * Java carregaria o histórico inteiro de vendas a cada clique no
 * gráfico, e o RNF0011 dá 1 segundo por consulta. O banco devolve no
 * máximo (categorias × meses) linhas, já somadas.
 *
 * Nativa, e não JPQL, porque o agrupamento precisa de
 * `date_trunc(... AT TIME ZONE ...)`: o mês de uma venda é o mês no fuso
 * da loja. Uma compra em 30/09 às 23h30 em São Paulo já é 1º de outubro
 * em UTC, e agrupar sem o fuso a jogaria no mês errado — o PED-90017 da
 * V1000 existe para provar isso.
 */
public interface AnaliseVendasRepository extends Repository<Pedido, Long> {

    /*
     * Valor vendido por categoria e por mês.
     *
     * - Só o status informado entra (o service passa ENTREGUE, critério
     *   (d) do enunciado). Fica como parâmetro, e não fixo no SQL, para o
     *   nome sair do enum StatusPedido e não de uma string solta.
     *
     * - O valor é o dos itens, quantidade × valor_unitario, que é a
     *   fotografia da compra (RF0038). O frete fica de fora: é do pedido
     *   inteiro e não pertence a nenhuma categoria.
     *
     * - O período é semiaberto, [inicio, fimExclusivo). O service manda o
     *   início do dia seguinte à data de fim, o que inclui o último dia
     *   inteiro sem depender de 23:59:59.999 — e é a forma que o índice
     *   ix_pedido_status_criado_em (V17) atende como intervalo.
     *
     * - Um instrumento em duas categorias (RN0012) entra nas duas. O total
     *   de cada categoria fica correto; somar categorias entre si, não.
     *
     * - Meses sem venda NÃO aparecem: o GROUP BY só produz linha onde há
     *   dado. Completar com zero é trabalho do service, que sabe quais
     *   meses o período cobre.
     *
     * - `categoriaIds` não pode vir vazio: `IN ()` é SQL inválido. O
     *   service exige ao menos uma categoria antes de chegar aqui.
     */
    @Query(value = """
            SELECT c.nome AS categoria,
                   to_char(date_trunc('month', p.criado_em AT TIME ZONE :fuso), 'YYYY-MM') AS mes,
                   sum(ip.quantidade * ip.valor_unitario) AS total
              FROM pedido p
              JOIN item_pedido ip           ON ip.pedido_id = p.id
              JOIN instrumento_categoria ic ON ic.instrumento_id = ip.instrumento_id
              JOIN categoria c              ON c.id = ic.categoria_id
             WHERE p.status = :status
               AND p.criado_em >= :inicio
               AND p.criado_em <  :fimExclusivo
               AND c.id IN (:categoriaIds)
             GROUP BY c.nome, mes
             ORDER BY c.nome, mes
            """, nativeQuery = true)
    List<VendaMensal> totaisMensaisPorCategoria(@Param("status") String status,
                                                @Param("fuso") String fuso,
                                                @Param("inicio") OffsetDateTime inicio,
                                                @Param("fimExclusivo") OffsetDateTime fimExclusivo,
                                                @Param("categoriaIds") Collection<Long> categoriaIds);

    /*
     * Uma linha do resultado. Projeção por interface: o Spring Data casa
     * cada getter com o alias da coluna (categoria, mes, total).
     *
     * O mês sai como texto "AAAA-MM", e não como data, porque é só uma
     * chave de agrupamento — o service o converte em YearMonth, e assim
     * nenhuma conversão de fuso acontece do lado do Java.
     */
    interface VendaMensal {

        String getCategoria();

        String getMes();

        BigDecimal getTotal();
    }
}
