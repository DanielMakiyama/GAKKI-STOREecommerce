package com.gakki.store.dto.response;

public record CartaoResponse(
        Long id,
        String apelido,
        String ultimosDigitos,
        String bandeira,
        String nomeTitular,
        Short validadeMes,
        Short validadeAno,
        boolean preferencial) {
}
