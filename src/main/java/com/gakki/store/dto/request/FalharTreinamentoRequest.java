package com.gakki.store.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FalharTreinamentoRequest(

        @NotBlank(message = "Informe o motivo da falha.")
        @Size(max = 1000, message = "O motivo tem no máximo 1000 caracteres.")
        String motivo) {
}
