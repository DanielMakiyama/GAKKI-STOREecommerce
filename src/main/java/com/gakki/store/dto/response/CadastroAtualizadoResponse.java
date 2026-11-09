package com.gakki.store.dto.response;

public record CadastroAtualizadoResponse(
        ClienteResponse cliente,
        String token,
        String refreshToken) {

    public static CadastroAtualizadoResponse semNovaSessao(ClienteResponse cliente) {
        return new CadastroAtualizadoResponse(cliente, null, null);
    }
}
