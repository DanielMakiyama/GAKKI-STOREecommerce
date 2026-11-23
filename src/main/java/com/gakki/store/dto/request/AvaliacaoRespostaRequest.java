package com.gakki.store.dto.request;

import com.gakki.store.domain.recomendacao.enums.AvaliacaoResposta;
import jakarta.validation.constraints.NotNull;

// RNF0044, critério d — útil ou não útil.
public record AvaliacaoRespostaRequest(

        @NotNull(message = "Informe se a resposta foi útil ou não.")
        AvaliacaoResposta avaliacao) {
}
