package com.gakki.store.exception;

/**
 * Estado atual do recurso impede a operação → HTTP 409.
 *
 * <p>Casos deste módulo: e-mail ou CPF já cadastrado, e remoção do último
 * endereço de entrega (RN0022) ou de cobrança (RN0021).
 */
public class ConflitoException extends RuntimeException {

    public ConflitoException(String mensagem) {
        super(mensagem);
    }
}
