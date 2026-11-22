package com.gakki.store.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// RNF0044 — pergunta feita pelo administrador em nome de um cliente.
public record SimulacaoIaRequest(

        @NotBlank(message = "Informe o e-mail do cliente.")
        @Email(message = "E-mail inválido.")
        String email,

        @NotBlank(message = "Informe a pergunta.")
        @Size(max = 1000, message = "A pergunta tem no máximo 1000 caracteres.")
        String pergunta) {
}
