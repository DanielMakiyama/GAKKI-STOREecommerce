package com.gakki.store.controller;

import com.gakki.store.dto.request.ConfirmarRecebimentoTrocaRequest;
import com.gakki.store.dto.request.SolicitarTrocaRequest;
import com.gakki.store.dto.response.PaginaResponse;
import com.gakki.store.dto.response.TrocaResponse;
import com.gakki.store.security.UsuarioAutenticado;
import com.gakki.store.service.TrocaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

// RF0041 a RF0045 — pedido de troca, do cliente ao cupom.

@RestController
@RequestMapping("/trocas")
@RequiredArgsConstructor
public class TrocaController {

    private final TrocaService trocaService;

    // RF0041 — solicitação do cliente. 201 com Location e corpo, pelo
    // mesmo motivo da finalização de compra: a tela precisa do número da
    // troca imediatamente.
    @PostMapping
    public ResponseEntity<TrocaResponse> solicitar(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @Valid @RequestBody SolicitarTrocaRequest requisicao) {

        TrocaResponse troca = trocaService.solicitar(usuario.getUsername(), requisicao);

        URI localizacao = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/trocas/{id}")
                .buildAndExpand(troca.id())
                .toUri();

        return ResponseEntity.created(localizacao).body(troca);
    }

    // As trocas de quem está no token. Sem id na URL: não há como pedir
    // as de outra pessoa.
    @GetMapping
    public ResponseEntity<PaginaResponse<TrocaResponse>> minhasTrocas(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @PageableDefault(size = 20) Pageable paginacao) {

        return ResponseEntity.ok(trocaService.minhasTrocas(usuario.getUsername(), paginacao));
    }

    // RF0043 — visualização de todas as trocas pelo administrador, em
    // rota própria. Mesmo critério de /pedidos/administracao.
    @GetMapping("/administracao")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaginaResponse<TrocaResponse>> trocasParaAdministracao(
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20) Pageable paginacao) {

        return ResponseEntity.ok(trocaService.trocasParaAdministracao(status, paginacao));
    }

    // RF0042 — autorização da troca, com a notificação da RN0046.
    @PostMapping("/{id}/autorizar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TrocaResponse> autorizar(@PathVariable Long id) {
        return ResponseEntity.ok(trocaService.autorizar(id));
    }

    // RF0044 e RF0045 — recebimento dos itens, decisão sobre o estoque e
    // emissão do cupom de troca.
    @PostMapping("/{id}/confirmar-recebimento")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TrocaResponse> confirmarRecebimento(
            @PathVariable Long id,
            @Valid @RequestBody ConfirmarRecebimentoTrocaRequest requisicao) {

        return ResponseEntity.ok(trocaService.confirmarRecebimento(id, requisicao.retornaEstoque()));
    }
}
