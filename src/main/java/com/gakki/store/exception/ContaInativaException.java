package com.gakki.store.exception;

/**
 * Credencial correta, cadastro inativado → HTTP 403.
 *
 * <p>Distinta da 401 de propósito: a senha conferiu, o que impede a
 * entrada é o estado do cadastro (RF0023). Devolver 401 aqui faria o
 * cliente pensar que errou a senha e tentar de novo indefinidamente.
 */
public class ContaInativaException extends RuntimeException {

    public ContaInativaException(String mensagem) {
        super(mensagem);
    }
}
