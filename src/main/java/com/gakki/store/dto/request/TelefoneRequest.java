package com.gakki.store.dto.request;

import com.gakki.store.domain.enums.TipoTelefone;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * RN0026 — o telefone é composto por tipo, DDD e número.
 *
 * <p>Por isso não é uma string única: o tipo é informação do domínio, e
 * uma máscara como "(11) 98888-7777" não carrega se o número é celular,
 * residencial ou comercial.
 */
public record TelefoneRequest(

        @NotNull(message = "Informe o tipo do telefone")
        TipoTelefone tipo,

        @NotNull(message = "Informe o DDD")
        @Pattern(regexp = "^[0-9]{2}$", message = "O DDD deve ter 2 dígitos")
        String ddd,

        @NotNull(message = "Informe o número do telefone")
        @Pattern(regexp = "^[0-9]{8,9}$", message = "O número deve ter 8 ou 9 dígitos, somente números")
        String numero) {
}
