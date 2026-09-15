package com.gakki.store.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Configuração do JWT, lida de {@code gakki.jwt} no application.yml.
 *
 * <p>O {@code secret} não tem valor padrão de propósito: a variável de
 * ambiente JWT_SECRET precisa existir, senão a aplicação não sobe. Uma
 * chave padrão no código seria a mesma em todo ambiente — e estaria no
 * Git, visível para qualquer um que clonasse o repositório.
 */
@ConfigurationProperties(prefix = "gakki.jwt")
public record JwtProperties(
        String secret,
        Duration accessTokenTtl,
        Duration refreshTokenTtl) {
}
