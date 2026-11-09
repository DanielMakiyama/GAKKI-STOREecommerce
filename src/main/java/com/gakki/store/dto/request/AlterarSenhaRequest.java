package com.gakki.store.dto.request;

import com.gakki.store.validation.SenhaForte;
import jakarta.validation.constraints.NotBlank;

// RF0028 - alteração isolada de senha, sem mexer no restante do cadastro.

public record AlterarSenhaRequest(

        @NotBlank(message = "Informe a senha atual")
        String senhaAtual,

        @NotBlank(message = "Informe a nova senha")
        @SenhaForte
        String novaSenha,

        @NotBlank(message = "Confirme a nova senha")
        String confirmacaoNovaSenha) {
}
