package com.gakki.store.dto.request;

import com.gakki.store.validation.SenhaForte;
import jakarta.validation.constraints.NotBlank;

/**
 * RF0028 — alteração isolada de senha, sem mexer no restante do cadastro.
 *
 * <p>A senha atual é exigida para que um token roubado não permita
 * trocar a senha e tomar a conta.
 */
public record AlterarSenhaRequest(

        @NotBlank(message = "Informe a senha atual")
        String senhaAtual,

        @NotBlank(message = "Informe a nova senha")
        @SenhaForte
        String novaSenha,

        @NotBlank(message = "Confirme a nova senha")
        String confirmacaoNovaSenha) {
}
