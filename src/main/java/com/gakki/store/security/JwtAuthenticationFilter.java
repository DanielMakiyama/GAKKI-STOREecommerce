package com.gakki.store.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// Lê o cabeçalho {@code Authorization: Bearer <token>} e popula o SecurityContext da requisição.

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String CABECALHO = "Authorization";
    private static final String PREFIXO = "Bearer ";

    private final JwtService jwtService;
    private final UsuarioDetailsService usuarioDetailsService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest requisicao,
                                    @NonNull HttpServletResponse resposta,
                                    @NonNull FilterChain cadeia) throws ServletException, IOException {

        String token = extrairToken(requisicao);

        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null
                && jwtService.ehTokenDeAcesso(token)) {

            String email = jwtService.extrairEmail(token);
            if (email != null) {
                autenticar(requisicao, email);
            }
        }

        cadeia.doFilter(requisicao, resposta);
    }

    private void autenticar(HttpServletRequest requisicao, String email) {
        UserDetails usuario = usuarioDetailsService.loadUserByUsername(email);

        // Conta inativada depois da emissão do token para de valer na
        // requisição seguinte (RF0023) — o token continua assinado, mas
        // o usuário não está mais habilitado.
        if (!usuario.isEnabled()) {
            return;
        }

        UsernamePasswordAuthenticationToken autenticacao =
                new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
        autenticacao.setDetails(new WebAuthenticationDetailsSource().buildDetails(requisicao));
        SecurityContextHolder.getContext().setAuthentication(autenticacao);
    }

    private String extrairToken(HttpServletRequest requisicao) {
        String cabecalho = requisicao.getHeader(CABECALHO);
        if (cabecalho == null || !cabecalho.startsWith(PREFIXO)) {
            return null;
        }
        String token = cabecalho.substring(PREFIXO.length()).trim();
        return token.isEmpty() ? null : token;
    }
}
