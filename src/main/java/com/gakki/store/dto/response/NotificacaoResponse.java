package com.gakki.store.dto.response;

import java.time.OffsetDateTime;

// RN0046 — notificação gerada ao cliente.
public record NotificacaoResponse(
        Long id,
        String mensagem,
        boolean lida,
        OffsetDateTime criadoEm) {
}
