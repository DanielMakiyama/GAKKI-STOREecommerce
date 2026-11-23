package com.gakki.store.dto.response;

import com.gakki.store.domain.recomendacao.enums.StatusModeloIa;

import java.time.OffsetDateTime;

// Uma versão do modelo. arquivoDados é a rota do JSONL usado no treino.
public record ModeloIaResponse(
        Integer versao,
        StatusModeloIa status,
        boolean ativo,
        String provedor,
        String modeloBase,
        String identificador,
        String jobId,
        int exemplosTotal,
        int exemplosVendas,
        int exemplosFeedback,
        OffsetDateTime dadosAte,
        OffsetDateTime iniciadoEm,
        OffsetDateTime concluidoEm,
        String erro,
        String arquivoDados) {
}
