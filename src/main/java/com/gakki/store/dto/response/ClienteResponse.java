package com.gakki.store.dto.response;

import com.gakki.store.domain.cliente.enums.Genero;
import com.gakki.store.domain.cliente.enums.Papel;

import java.time.LocalDate;

// Cadastro completo do cliente.
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
