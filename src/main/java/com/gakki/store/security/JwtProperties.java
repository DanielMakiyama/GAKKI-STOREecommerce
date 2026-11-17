package com.gakki.store.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

// Configuração do JWT, lida de {@code gakki.jwt} no application.yml.

@ConfigurationProperties(prefix = "gakki.jwt")
public record JwtProperties(
        String secret,
        Duration accessTokenTtl,
        Duration refreshTokenTtl) {
}
