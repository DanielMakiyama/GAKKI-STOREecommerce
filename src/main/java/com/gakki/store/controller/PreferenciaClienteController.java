package com.gakki.store.controller;

import com.gakki.store.dto.request.PreferenciaClienteRequest;
import com.gakki.store.dto.response.PreferenciaClienteResponse;
import com.gakki.store.security.UsuarioAutenticado;
import com.gakki.store.service.PreferenciaClienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// RNF0044 — preferências musicais do cliente autenticado.
@RestController
@RequestMapping("/clientes/me/preferencias")
@PreAuthorize("hasRole('CLIENTE')")
@RequiredArgsConstructor
public class PreferenciaClienteController {

    private final PreferenciaClienteService preferenciaService;

    // 204 enquanto o cliente não preencheu o formulário.
    @GetMapping
    public ResponseEntity<PreferenciaClienteResponse> buscar(
            @AuthenticationPrincipal UsuarioAutenticado usuario) {

        return preferenciaService.buscar(usuario.getUsername())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    // PUT porque o recurso é único por cliente: criar e alterar são a mesma operação.
    @PutMapping
    public ResponseEntity<PreferenciaClienteResponse> salvar(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @Valid @RequestBody PreferenciaClienteRequest requisicao) {

        return ResponseEntity.ok(preferenciaService.salvar(usuario.getUsername(), requisicao));
    }
}
