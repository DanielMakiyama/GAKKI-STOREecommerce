package com.gakki.store.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// identificador: nome do modelo ajustado no Ollama, ex.: gakki-v1.
public record ConcluirTreinamentoRequest(

        @NotBlank(message = "Informe o nome do modelo criado no Ollama.")
        @Size(max = 300, message = "O nome tem no máximo 300 caracteres.")
        String identificador) {
}
