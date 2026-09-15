package com.gakki.store.exception;

/**
 * Regra de negócio violada por dados que a validação declarativa não
 * alcança → HTTP 400.
 *
 * <p>Bean Validation checa um campo de cada vez. Regras que dependem de
 * dois campos (senha × confirmação) ou do estado do banco (bandeira
 * ativa) só podem ser verificadas no service, e caem aqui.
 */
public class RegraDeNegocioException extends RuntimeException {

    public RegraDeNegocioException(String mensagem) {
        super(mensagem);
    }
}
