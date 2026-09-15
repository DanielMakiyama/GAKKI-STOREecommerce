package com.gakki.store.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

/**
 * Emissão e leitura dos tokens JWT.
 *
 * <p>Dois tipos de token, distinguidos pela claim {@code tipo}: o de
 * acesso é curto e acompanha toda requisição; o de renovação é longo e
 * só serve para obter um novo par. Sem essa marcação, um token de
 * acesso roubado poderia ser usado para renovar a sessão
 * indefinidamente.
 */
@Service
public class JwtService {

    private static final String CLAIM_PAPEL = "papel";
    private static final String CLAIM_USUARIO_ID = "uid";
    private static final String CLAIM_TIPO = "tipo";
    private static final String TIPO_ACESSO = "access";
    private static final String TIPO_RENOVACAO = "refresh";

    private final SecretKey chave;
    private final Duration ttlAcesso;
    private final Duration ttlRenovacao;

    public JwtService(JwtProperties propriedades) {
        byte[] bytes = propriedades.secret().getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException(
                    "JWT_SECRET precisa ter no mínimo 32 bytes para HS256. "
                            + "Gere uma chave com: openssl rand -base64 48");
        }
        this.chave = Keys.hmacShaKeyFor(bytes);
        this.ttlAcesso = propriedades.accessTokenTtl();
        this.ttlRenovacao = propriedades.refreshTokenTtl();
    }

    public String gerarTokenDeAcesso(UsuarioAutenticado usuario) {
        return gerar(usuario, TIPO_ACESSO, ttlAcesso);
    }

    public String gerarTokenDeRenovacao(UsuarioAutenticado usuario) {
        return gerar(usuario, TIPO_RENOVACAO, ttlRenovacao);
    }

    public Duration getTtlAcesso() {
        return ttlAcesso;
    }

    /** E-mail do dono do token, ou {@code null} se o token for inválido ou expirado. */
    public String extrairEmail(String token) {
        Claims claims = ler(token);
        return claims == null ? null : claims.getSubject();
    }

    public boolean ehTokenDeAcesso(String token) {
        return ehDoTipo(token, TIPO_ACESSO);
    }

    public boolean ehTokenDeRenovacao(String token) {
        return ehDoTipo(token, TIPO_RENOVACAO);
    }

    private String gerar(UsuarioAutenticado usuario, String tipo, Duration validade) {
        Instant agora = Instant.now();
        return Jwts.builder()
                .subject(usuario.getUsername())
                .claim(CLAIM_USUARIO_ID, usuario.getId())
                .claim(CLAIM_PAPEL, usuario.getPapel().name())
                .claim(CLAIM_TIPO, tipo)
                .issuedAt(Date.from(agora))
                .expiration(Date.from(agora.plus(validade)))
                .signWith(chave, Jwts.SIG.HS256)
                .compact();
    }

    private boolean ehDoTipo(String token, String tipo) {
        Claims claims = ler(token);
        return claims != null && tipo.equals(claims.get(CLAIM_TIPO, String.class));
    }

    /**
     * Devolve as claims ou {@code null}.
     *
     * <p>Token inválido não é situação excepcional numa API pública —
     * expira o tempo todo. Propagar exceção daqui obrigaria cada
     * chamador a um try/catch; devolver null deixa a decisão com quem
     * chamou, que responde 401 e segue.
     */
    private Claims ler(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(chave)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }
}
