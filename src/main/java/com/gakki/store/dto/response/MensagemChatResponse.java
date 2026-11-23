package com.gakki.store.dto.response;

import com.gakki.store.domain.recomendacao.enums.AutorMensagem;
import com.gakki.store.domain.recomendacao.enums.AvaliacaoResposta;

import java.time.OffsetDateTime;
import java.util.List;

// avaliacao é nula enquanto o cliente não avaliou (e sempre nas mensagens dele).
public record MensagemChatResponse(
        Long id,
        AutorMensagem autor,
        String texto,
        OffsetDateTime criadoEm,
        AvaliacaoResposta avaliacao,
        List<ProdutoCitadoResponse> produtos) {
}
