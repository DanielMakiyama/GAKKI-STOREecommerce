package com.gakki.store.dto.request;

import com.gakki.store.domain.cliente.enums.Genero;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.br.CPF;

import java.time.LocalDate;

// RF0022 - alteração dos dados cadastrais.

public record AtualizarClienteRequest(

        @NotBlank(message = "Informe o nome completo")
        @Size(max = 150, message = "O nome deve ter no máximo 150 caracteres")
        String nome,

        @NotBlank(message = "Informe o e-mail")
        @Email(message = "E-mail inválido")
        @Size(max = 150, message = "O e-mail deve ter no máximo 150 caracteres")
        String email,

        // @CPF é do Hibernate Validator e confere os dígitos
        // verificadores, não apenas o formato: "111.111.111-11" é
        // recusado mesmo tendo 11 dígitos.
        @NotBlank(message = "Informe o CPF")
        @CPF(message = "CPF inválido")
        String cpf,

        @NotNull(message = "Informe o gênero")
        Genero genero,

        @NotNull(message = "Informe a data de nascimento")
        @Past(message = "A data de nascimento deve ser anterior a hoje")
        LocalDate dataNascimento,

        @NotNull(message = "Informe o telefone")
        @Valid
        TelefoneRequest telefone) {
}
