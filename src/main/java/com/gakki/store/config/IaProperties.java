package com.gakki.store.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

// RNF0044 — conexão com o modelo de IA, lida de gakki.ia no application.yml.
@ConfigurationProperties(prefix = "gakki.ia")
public record IaProperties(
        String url,
        String modeloBase,
        Integer numGpu,
        Integer numCtx,
        Integer maxTokens,
        Double temperatura,
        String keepAlive,
        Duration timeout,
        boolean aquecerNaInicializacao) {
}
