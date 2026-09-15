package com.gakki.store.exception;

/**
 * Credencial não confere → HTTP 401.
 *
 * <p>Vale para o login e para a senha atual informada na troca de senha
 * (RF0028). A mensagem é deliberadamente genérica: dizer "e-mail não
 * existe" entregaria a quem está tentando adivinhar quais contas existem.
 */
public class CredencialInvalidaException extends RuntimeException {

    public CredencialInvalidaException(String mensagem) {
        super(mensagem);
    }
}
