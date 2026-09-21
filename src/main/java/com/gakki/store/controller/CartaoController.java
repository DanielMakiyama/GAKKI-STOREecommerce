package com.gakki.store.controller;

import com.gakki.store.dto.request.CartaoRequest;
import com.gakki.store.dto.response.CartaoResponse;
import com.gakki.store.security.UsuarioAutenticado;
import com.gakki.store.service.CartaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

//RF0027 — cartões de crédito do cliente autenticado

@RestController
@RequestMapping("/clientes/me/cartoes")
@RequiredArgsConstructor
public class CartaoController {

    private final CartaoService cartaoService;

    @GetMapping
    public ResponseEntity<List<CartaoResponse>> listar(
            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ResponseEntity.ok(cartaoService.listar(usuario.getUsername()));
    }

    @PostMapping
    public ResponseEntity<CartaoResponse> adicionar(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @Valid @RequestBody CartaoRequest requisicao) {

        CartaoResponse cartao = cartaoService.adicionar(usuario.getUsername(), requisicao);

        URI localizacao = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(cartao.id())
                .toUri();

        return ResponseEntity.created(localizacao).body(cartao);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CartaoResponse> atualizar(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @PathVariable Long id,
            @Valid @RequestBody CartaoRequest requisicao) {

        return ResponseEntity.ok(cartaoService.atualizar(usuario.getUsername(), id, requisicao));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @PathVariable Long id) {

        cartaoService.remover(usuario.getUsername(), id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/preferencial")
    public ResponseEntity<Void> definirPreferencial(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @PathVariable Long id) {

        cartaoService.definirPreferencial(usuario.getUsername(), id);
        return ResponseEntity.noContent().build();
    }
}
