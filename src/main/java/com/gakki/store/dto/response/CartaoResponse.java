package com.gakki.store.dto.response;

/**
 * Cartão do cliente.
 *
 * <p>A bandeira sai como nome, não como id: a tela exibe "Visa", e
 * obrigá-la a cruzar o id com a lista de bandeiras para descobrir isso
 * seria trabalho que pertence ao backend.
 */
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
