package com.gakki.store.exception;

// Falha ao falar com o provedor de IA. Não chega ao cliente: o service
// captura e responde que o assistente está indisponível.
public class IaIndisponivelException extends RuntimeException {

    public IaIndisponivelException(String mensagem) {
        super(mensagem);
    }

    public IaIndisponivelException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
