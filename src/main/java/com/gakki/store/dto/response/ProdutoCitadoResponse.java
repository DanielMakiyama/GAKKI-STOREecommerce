package com.gakki.store.dto.response;

import java.math.BigDecimal;

// Produto do catálogo citado numa resposta do assistente; a tela vira link.
public record ProdutoCitadoResponse(Long id, String codigo, String nome, BigDecimal preco) {
}
