package com.gakki.store.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

//RN0024 / RN0025 — cartão de crédito do cliente.

public record CartaoRequest(

        @NotBlank(message = "Informe um apelido para o cartão")
        @Size(max = 40, message = "O apelido deve ter no máximo 40 caracteres")
        String apelido,

        @NotBlank(message = "Informe os quatro últimos dígitos do cartão")
        @Pattern(regexp = "^[0-9]{4}$", message = "Informe exatamente os quatro últimos dígitos")
        String ultimosDigitos,

        @NotNull(message = "Informe a bandeira do cartão")
        Long bandeiraId,

        @NotBlank(message = "Informe o nome impresso no cartão")
        @Size(max = 100, message = "O nome do titular deve ter no máximo 100 caracteres")
        String nomeTitular,

        @NotNull(message = "Informe o mês de validade")
        @Min(value = 1, message = "O mês de validade deve estar entre 1 e 12")
        @Max(value = 12, message = "O mês de validade deve estar entre 1 e 12")
        Short validadeMes,

        @NotNull(message = "Informe o ano de validade")
        @Min(value = 2000, message = "Ano de validade inválido")
        @Max(value = 2100, message = "Ano de validade inválido")
        Short validadeAno) {
}
