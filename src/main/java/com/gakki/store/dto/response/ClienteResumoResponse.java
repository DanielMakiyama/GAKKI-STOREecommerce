package com.gakki.store.dto.response;

/**
 * Linha da listagem administrativa (RF0024).
 *
 * <p>Versão enxuta do cadastro: uma página de 20 clientes não precisa
 * carregar gênero, data de nascimento e telefone de cada um para
 * desenhar uma tabela com nome, e-mail e status.
 */
public record ClienteResumoResponse(
        Long id,
        String codigo,
        String nome,
        String email,
        String cpf,
        boolean ativo) {
}
