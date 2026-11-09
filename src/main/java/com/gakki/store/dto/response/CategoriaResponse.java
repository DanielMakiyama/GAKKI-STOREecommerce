package com.gakki.store.dto.response;

/*
 * Categoria de instrumento (RN0012), para as listas de seleção.
 *
 * Mesmo formato do `db.categorias` do mock: a tela de análise de vendas
 * e os filtros do catálogo já leem `id` e `nome`.
 */
public record CategoriaResponse(Long id, String nome) {
}
