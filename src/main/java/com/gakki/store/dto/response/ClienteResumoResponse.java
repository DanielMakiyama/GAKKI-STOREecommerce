package com.gakki.store.dto.response;

// Linha da listagem administrativa (RF0024).

public record ClienteResumoResponse(
        Long id,
        String codigo,
        String nome,
        String email,
        String cpf,
        boolean ativo) {
}
