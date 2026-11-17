package com.gakki.store.dto.response;

import com.gakki.store.domain.cliente.enums.Papel;

// Resposta da autenticação.
public record LoginResponse(
        String token,
        String refreshToken,
        long expiraEmSegundos,
        String nome,
        Papel perfil) {
}
