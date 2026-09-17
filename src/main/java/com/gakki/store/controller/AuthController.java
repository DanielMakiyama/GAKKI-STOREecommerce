package com.gakki.store.controller;

import com.gakki.store.dto.request.LoginRequest;
import com.gakki.store.dto.request.RegistrarClienteRequest;
import com.gakki.store.dto.request.RenovarTokenRequest;
import com.gakki.store.dto.response.ClienteResponse;
import com.gakki.store.dto.response.LoginResponse;
import com.gakki.store.service.AuthService;
import com.gakki.store.service.ClienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

/**
 * Endpoints públicos de entrada no sistema.
 *
 * <p>O caminho é relativo ao context-path {@code /api/v1}, definido no
 * application.yml — o prefixo de versão fica declarado num lugar só.
 *
 * <p>O controller não tem regra nenhuma: valida o formato do corpo com
 * {@code @Valid}, delega ao service e traduz o retorno em status HTTP.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final ClienteService clienteService;
    private final AuthService authService;

    /**
     * RF0021 — cadastro de cliente.
     *
     * <p>Responde 201 com o cabeçalho {@code Location} apontando para o
     * recurso criado, como manda o HTTP para criação bem-sucedida.
     */
    @PostMapping("/registrar")
    public ResponseEntity<ClienteResponse> registrar(@Valid @RequestBody RegistrarClienteRequest requisicao) {
        ClienteResponse cliente = clienteService.cadastrar(requisicao);

        URI localizacao = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/clientes/{id}")
                .buildAndExpand(cliente.id())
                .toUri();

        return ResponseEntity.created(localizacao).body(cliente);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest requisicao) {
        return ResponseEntity.ok(authService.autenticar(requisicao));
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> renovar(@Valid @RequestBody RenovarTokenRequest requisicao) {
        return ResponseEntity.ok(authService.renovar(requisicao));
    }
}
