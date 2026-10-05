package com.gakki.store.dto.response;

import java.math.BigDecimal;
import java.util.List;

// Instrumento como o catálogo o consome (RF0011).
//
// `fabricante` e `categorias` saem como NOMES, não como ids: é o que a
// tela imprime, e o mock do front já fazia assim. Devolver id obrigaria
// cada card do catálogo a fazer outra consulta para descobrir o nome.
//
// `quantidadeEstoque` vai junto porque a tela de produto usa o estoque
// como limite do seletor de quantidade — é o que evita o cliente pedir
// mais do que existe e só descobrir no 400 da RN0031.

public record InstrumentoResponse(
        Long id,
        String codigo,
        String nome,
        String descricao,
        String fabricante,
        Short anoFabricacao,
        BigDecimal valorVenda,
        Integer quantidadeEstoque,
        boolean ativo,
        List<String> categorias) {
}
