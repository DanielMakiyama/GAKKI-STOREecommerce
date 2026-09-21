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

// Rotas públicas. O /api/v1 vem do context-path no application.yml.

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final ClienteService clienteService;
    private final AuthService authService;

    // RF0021. 201 + Location porque um recurso novo passou a existir.

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
