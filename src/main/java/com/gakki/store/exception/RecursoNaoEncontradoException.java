package com.gakki.store.exception;

/** Recurso inexistente ou fora do alcance do usuário autenticado → HTTP 404. */
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }

    public static RecursoNaoEncontradoException cliente(Long id) {
        return new RecursoNaoEncontradoException("Cliente não encontrado: " + id);
    }

    public static RecursoNaoEncontradoException endereco(Long id) {
        return new RecursoNaoEncontradoException("Endereço não encontrado: " + id);
    }

    public static RecursoNaoEncontradoException cartao(Long id) {
        return new RecursoNaoEncontradoException("Cartão não encontrado: " + id);
    }
}
