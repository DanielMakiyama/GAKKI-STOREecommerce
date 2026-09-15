package com.gakki.store.domain.enums;

/**
 * Tipo do telefone.
 *
 * <p>A RN0026 exige que o telefone seja composto por tipo, DDD e número —
 * por isso ele não é uma string única no cadastro.
 */
public enum TipoTelefone {
    CELULAR,
    RESIDENCIAL,
    COMERCIAL
}
