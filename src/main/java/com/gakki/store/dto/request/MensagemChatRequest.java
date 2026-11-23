package com.gakki.store.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// RNF0044 — mensagem do cliente para o chatbot.
public record MensagemChatRequest(

        @NotBlank(message = "Escreva uma mensagem.")
        @Size(max = 1000, message = "A mensagem tem no máximo 1000 caracteres.")
        String texto) {
}
