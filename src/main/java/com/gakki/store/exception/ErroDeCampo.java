package com.gakki.store.exception;

/**
 * Um campo inválido dentro da resposta 400.
 *
 * <p>Vai na propriedade {@code errors} do ProblemDetail, para o front
 * conseguir destacar o campo específico em vez de exibir uma frase solta.
 */
public record ErroDeCampo(String campo, String mensagem) {
}
