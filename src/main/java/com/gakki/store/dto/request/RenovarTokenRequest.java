package com.gakki.store.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RenovarTokenRequest(

        @NotBlank(message = "Informe o token de renovação")
        String refreshToken) {
}
