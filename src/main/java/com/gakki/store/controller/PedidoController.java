package com.gakki.store.controller;

import com.gakki.store.dto.request.FinalizarCompraRequest;
import com.gakki.store.dto.response.PaginaResponse;
import com.gakki.store.dto.response.PedidoResponse;
import com.gakki.store.security.UsuarioAutenticado;
import com.gakki.store.service.PedidoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

// RF0038 — pedidos do cliente autenticado.

@RestController
@RequestMapping("/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;

    // RF0038 — finalização da compra. 201 com Location, como toda criação
    // de recurso no projeto.
    //
    // O corpo vem junto porque a tela de confirmação precisa do número do
    // pedido, do status e do eventual cupom de troco imediatamente — um
    // 201 vazio obrigaria a um GET logo em seguida.
    @PostMapping
    public ResponseEntity<PedidoResponse> finalizarCompra(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @Valid @RequestBody FinalizarCompraRequest requisicao) {

        PedidoResponse pedido = pedidoService.finalizarCompra(usuario.getUsername(), requisicao);

        URI localizacao = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/pedidos/{id}")
                .buildAndExpand(pedido.id())
                .toUri();

        return ResponseEntity.created(localizacao).body(pedido);
    }

    // Paginada, como toda listagem do projeto. O mock devolvia um array
    // puro: o Pedidos.jsx vai precisar ler `.content`, mesma adaptação que
    // o painel administrativo já levou.
    @GetMapping
    public ResponseEntity<PaginaResponse<PedidoResponse>> meusPedidos(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @PageableDefault(size = 20) Pageable paginacao) {

        return ResponseEntity.ok(pedidoService.meusPedidos(usuario.getUsername(), paginacao));
    }
}
