package com.gakki.store.dto.response;

// resposta é nula quando a IA não respondeu; aviso explica ao cliente.
public record RespostaChatResponse(
        MensagemChatResponse pergunta,
        MensagemChatResponse resposta,
        String aviso) {
}
