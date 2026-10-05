package com.gakki.store.dto.response;

import com.gakki.store.domain.cliente.enums.TipoTelefone;

public record TelefoneResponse(TipoTelefone tipo, String ddd, String numero) {
}
