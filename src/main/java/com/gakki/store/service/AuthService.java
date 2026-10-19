package com.gakki.store.service;

import com.gakki.store.domain.cliente.Cliente;
import com.gakki.store.dto.request.LoginRequest;
import com.gakki.store.dto.request.RenovarTokenRequest;
import com.gakki.store.dto.response.LoginResponse;
import com.gakki.store.exception.ContaInativaException;
import com.gakki.store.exception.CredencialInvalidaException;
import com.gakki.store.repository.ClienteRepository;
import com.gakki.store.security.JwtService;
import com.gakki.store.security.UsuarioAutenticado;
import com.gakki.store.security.UsuarioDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UsuarioDetailsService usuarioDetailsService;
    private final ClienteRepository clienteRepository;
    private final JwtService jwtService;

    //Login

    @Transactional(readOnly = true)
    public LoginResponse autenticar(LoginRequest requisicao) {
        String email = requisicao.email().trim().toLowerCase(Locale.ROOT);

        UsuarioAutenticado usuario;
        try {
            var autenticacao = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, requisicao.senha()));
            usuario = (UsuarioAutenticado) autenticacao.getPrincipal();
        } catch (DisabledException e) {
            throw new ContaInativaException(
                    "Este cadastro está inativo. Procure o suporte para reativá-lo.");
        } catch (AuthenticationException e) {
            // Mensagem propositalmente genérica: dizer "e-mail não
            // existe" entregaria quais contas existem no sistema.
            throw new CredencialInvalidaException("E-mail ou senha inválidos.");
        }

        return montarResposta(usuario);
    }

    //troca o token de renovação por um par novo

    @Transactional(readOnly = true)
    public LoginResponse renovar(RenovarTokenRequest requisicao) {
        String token = requisicao.refreshToken();

        if (!jwtService.ehTokenDeRenovacao(token)) {
            throw new CredencialInvalidaException("Token de renovação inválido ou expirado.");
        }

        String email = jwtService.extrairEmail(token);
        if (email == null) {
            throw new CredencialInvalidaException("Token de renovação inválido ou expirado.");
        }

        UsuarioAutenticado usuario = (UsuarioAutenticado) usuarioDetailsService.loadUserByUsername(email);

        // Conta inativada depois da emissão do token para de renovar.
        if (!usuario.isEnabled()) {
            throw new ContaInativaException("Este cadastro está inativo.");
        }

        return montarResposta(usuario);
    }

    // Emite uma sessão nova para um usuário já identificado.

    @Transactional(readOnly = true)
    public LoginResponse emitirSessao(String email) {
        return montarResposta((UsuarioAutenticado) usuarioDetailsService.loadUserByUsername(email));
    }

    private LoginResponse montarResposta(UsuarioAutenticado usuario) {
        return new LoginResponse(
                jwtService.gerarTokenDeAcesso(usuario),
                jwtService.gerarTokenDeRenovacao(usuario),
                jwtService.getTtlAcesso().toSeconds(),
                nomeDeExibicao(usuario),
                usuario.getPapel());
    }

    // Nome mostrado no cabeçalho do front.

    private String nomeDeExibicao(UsuarioAutenticado usuario) {
        return clienteRepository.findByUsuarioEmail(usuario.getUsername())
                .map(Cliente::getNome)
                .orElseGet(() -> usuario.getUsername().split("@")[0]);
    }
}
