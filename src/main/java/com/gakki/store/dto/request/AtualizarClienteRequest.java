package com.gakki.store.dto.request;

import com.gakki.store.domain.enums.Genero;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * RF0022 — alteração dos dados cadastrais.
 *
 * <p>E-mail e CPF ficam de fora: o e-mail é a credencial de login e o
 * CPF identifica a pessoa. Alterar qualquer um dos dois é outro caso de
 * uso, com outras consequências, e não "editar cadastro".
 *
 * <p>Senha também não entra aqui — a RF0028 exige que ela seja alterável
 * isoladamente, e a RNF0034 diz o mesmo sobre endereços.
 */
public record AtualizarClienteRequest(

        @NotBlank(message = "Informe o nome completo")
        @Size(max = 150, message = "O nome deve ter no máximo 150 caracteres")
        String nome,

        @NotNull(message = "Informe o gênero")
        Genero genero,

        @NotNull(message = "Informe a data de nascimento")
        @Past(message = "A data de nascimento deve ser anterior a hoje")
        LocalDate dataNascimento,

        @NotNull(message = "Informe o telefone")
        @Valid
        TelefoneRequest telefone) {
}
