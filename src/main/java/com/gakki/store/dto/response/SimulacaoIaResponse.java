package com.gakki.store.dto.response;

// instrucoesDoSistema é o texto exato enviado à IA antes da pergunta.
public record SimulacaoIaResponse(
        String resposta,
        String modelo,
        Integer versaoAjustada,
        long tempoMs,
        String instrucoesDoSistema) {
}
