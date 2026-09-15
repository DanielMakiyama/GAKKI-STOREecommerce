package com.gakki.store.dto.response;

import com.gakki.store.domain.enums.Genero;
import com.gakki.store.domain.enums.Papel;

import java.time.LocalDate;

/**
 * Cadastro completo do cliente.
 *
 * <p>O CPF sai mascarado ({@code ***.456.789-**}). É dado pessoal
 * sensível, nenhuma tela precisa dele por inteiro, e o que não sai da
 * API não vaza em log de navegador nem em captura de tela.
 *
 * <p>Não existe campo de senha aqui — nem o hash. É a razão de a
 * entidade nunca virar resposta HTTP: bastaria um {@code @JsonIgnore}
 * esquecido para o hash ir junto.
 */
public record ClienteResponse(
        Long id,
        String codigo,
        String nome,
        String email,
        String cpf,
        Genero genero,
        LocalDate dataNascimento,
        TelefoneResponse telefone,
        Papel perfil,
        boolean ativo) {
}
