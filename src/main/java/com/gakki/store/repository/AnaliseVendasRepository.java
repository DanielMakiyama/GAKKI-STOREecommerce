package com.gakki.store.repository;

import com.gakki.store.domain.vendas.Pedido;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;

// Histórico de vendas por categoria (RF0055, RNF0043, RNF0011).

public interface AnaliseVendasRepository extends Repository<Pedido, Long> {

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

    interface VendaMensal {

        String getCategoria();

        String getMes();

        BigDecimal getTotal();
    }
}
