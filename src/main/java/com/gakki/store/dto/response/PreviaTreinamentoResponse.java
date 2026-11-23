package com.gakki.store.dto.response;

import java.time.OffsetDateTime;

// O que entraria num treino agora e o que chegou depois da última versão.
public record PreviaTreinamentoResponse(
        int exemplosVendas,
        int exemplosFeedback,
        int exemplosCatalogo,
        int exemplosTotal,
        Integer ultimaVersao,
        OffsetDateTime ultimoCorte,
        long pedidosNovos,
        long avaliacoesNovas) {
}
