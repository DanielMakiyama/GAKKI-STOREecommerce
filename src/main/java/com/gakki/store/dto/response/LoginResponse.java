package com.gakki.store.dto.response;

import com.gakki.store.domain.enums.Papel;

/**
 * Resposta da autenticação.
 *
 * <p>{@code token}, {@code nome} e {@code perfil} são os campos que o
 * front já consome hoje; {@code refreshToken} e {@code expiraEmSegundos}
 * são adicionais e serão ignorados sem quebrar nada até a tela passar a
 * usá-los.
 */
public record LoginResponse(
        String token,
        String refreshToken,
        long expiraEmSegundos,
        String nome,
        Papel perfil) {
}
