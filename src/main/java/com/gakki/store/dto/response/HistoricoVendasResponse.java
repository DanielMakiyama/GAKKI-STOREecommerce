package com.gakki.store.dto.response;

import java.math.BigDecimal;

/*
 * RF0055 e RNF0043 — um ponto do gráfico: quanto uma categoria vendeu
 * num mês.
 *
 * Os nomes e o formato são os que o mock do front já devolvia
 * (`historicoVendas` do mockApi.js): uma lista plana, uma linha por
 * categoria e mês. A tela agrupa pelo `mesAno` para desenhar o eixo
 * horizontal e por `categoria` para desenhar as linhas — devolver o
 * gráfico "pronto" obrigaria a mexer no componente sem ganho.
 *
 * `mesAno` no formato MM/AAAA ("09/2026"), o mesmo do mock e o rótulo
 * que o eixo mostra. `totalVendas` em reais, com duas casas, inclusive
 * nos meses sem venda (0.00): a linha do gráfico precisa do ponto zero,
 * e não de um buraco.
 */
public record HistoricoVendasResponse(
        String categoria,
        String mesAno,
        BigDecimal totalVendas) {
}
