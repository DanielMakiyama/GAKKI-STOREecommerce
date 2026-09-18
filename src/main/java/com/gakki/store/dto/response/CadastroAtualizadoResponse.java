package com.gakki.store.dto.response;

/**
 * Resposta do {@code PUT /clientes/me} (RF0022).
 *
 * <p>Existe por causa de uma consequência da alteração de e-mail: o
 * {@code subject} do JWT é o e-mail, então, no instante em que ele muda,
 * o token que o cliente tem na mão deixa de resolver para um usuário e a
 * requisição seguinte volta 401 — sem motivo aparente para quem está
 * usando a tela.
 *
 * <p>Por isso a resposta pode trazer uma sessão nova. {@code token} e
 * {@code refreshToken} vêm preenchidos <b>apenas</b> quando o e-mail
 * mudou; nos demais casos são {@code null} e a tela não faz nada.
 * Renovar sempre também funcionaria, mas trocaria a credencial do
 * cliente a cada correção de telefone, sem necessidade.
 */
public record CadastroAtualizadoResponse(
        ClienteResponse cliente,
        String token,
        String refreshToken) {

    public static CadastroAtualizadoResponse semNovaSessao(ClienteResponse cliente) {
        return new CadastroAtualizadoResponse(cliente, null, null);
    }
}
