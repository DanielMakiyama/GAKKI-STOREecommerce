package com.gakki.store.controller;

import com.gakki.store.dto.request.EnderecoRequest;
import com.gakki.store.dto.response.EnderecoResponse;
import com.gakki.store.security.UsuarioAutenticado;
import com.gakki.store.service.EnderecoService;
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

/**
 * RF0026 — endereços do cliente autenticado.
 *
 * <p>Controller separado do {@code ClienteController} porque o recurso é
 * outro: endereço tem ciclo de vida próprio, é criado e excluído por
 * conta própria, e a RNF0034 exige justamente que isso aconteça sem
 * passar pela alteração do cadastro.
 */
@RestController
@RequestMapping("/clientes/me/enderecos")
@RequiredArgsConstructor
public class EnderecoController {

    private final EnderecoService enderecoService;

    @GetMapping
    public ResponseEntity<List<EnderecoResponse>> listar(
            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ResponseEntity.ok(enderecoService.listar(usuario.getUsername()));
    }

    @PostMapping
    public ResponseEntity<EnderecoResponse> adicionar(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @Valid @RequestBody EnderecoRequest requisicao) {

        EnderecoResponse endereco = enderecoService.adicionar(usuario.getUsername(), requisicao);

        URI localizacao = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(endereco.id())
                .toUri();

        return ResponseEntity.created(localizacao).body(endereco);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EnderecoResponse> atualizar(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @PathVariable Long id,
            @Valid @RequestBody EnderecoRequest requisicao) {

        return ResponseEntity.ok(enderecoService.atualizar(usuario.getUsername(), id, requisicao));
    }

    /**
     * Exclusão física — 409 quando violaria a RN0021 ou a RN0022.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @PathVariable Long id) {

        enderecoService.remover(usuario.getUsername(), id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/principal")
    public ResponseEntity<Void> definirPrincipal(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @PathVariable Long id) {

        enderecoService.definirPrincipal(usuario.getUsername(), id);
        return ResponseEntity.noContent().build();
    }
}
