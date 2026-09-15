package com.gakki.store.dto.request;

import com.gakki.store.domain.enums.Genero;
import com.gakki.store.validation.SenhaForte;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.br.CPF;

import java.time.LocalDate;

/**
 * RF0021 — cadastro de cliente, com os dados obrigatórios da RN0026.
 *
 * <p>O endereço vem no mesmo payload, e não numa segunda requisição: a
 * RN0022 exige que todo cliente tenha endereço de entrega, e duas
 * chamadas deixariam no banco exatamente o que a regra proíbe caso a
 * segunda falhasse. Cartão é opcional — nenhuma regra condiciona a
 * existência do cliente a ter cartão.
 */
public record RegistrarClienteRequest(

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
        TelefoneRequest telefone,

        @NotBlank(message = "Informe a senha")
        @SenhaForte
        String senha,

        // RNF0032 — a igualdade entre os dois é conferida no service:
        // Bean Validation enxerga um campo por vez e não compara dois.
        @NotBlank(message = "Confirme a senha")
        String confirmacaoSenha,

        @NotNull(message = "Informe o endereço de entrega")
        @Valid
        EnderecoRequest endereco,

        @Valid
        CartaoRequest cartao) {
}
