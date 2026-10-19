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

// RF0038 a RF0040 — pedidos do cliente e ciclo de vida administrado.

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
    // puro: o Pedidos.jsx lê `.content`, mesma adaptação que o painel
    // administrativo já levou.
    @GetMapping
    public ResponseEntity<PaginaResponse<PedidoResponse>> meusPedidos(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @PageableDefault(size = 20) Pageable paginacao) {

        return ResponseEntity.ok(pedidoService.meusPedidos(usuario.getUsername(), paginacao));
    }

    /*
     * RF0039 — a fila do administrador, em rota própria.
     *
     * Poderia ser o mesmo GET /pedidos devolvendo coisas diferentes
     * conforme o papel de quem pergunta, mas o projeto já decidiu o
     * contrário quando separou /clientes/me de /clientes: a rota diz o
     * escopo, e não o token. Uma rota que muda de significado conforme
     * quem chama é a que mais vaza dado por engano.
     */
    @GetMapping("/administracao")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaginaResponse<PedidoResponse>> pedidosParaAdministracao(
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20) Pageable paginacao) {

        return ResponseEntity.ok(pedidoService.pedidosParaAdministracao(status, paginacao));
    }

    /*
     * RN0037 e RN0038 — submete a forma de pagamento à operadora e grava
     * APROVADA ou REPROVADA conforme o retorno. Ponto de extensão PE1 do
     * CDU02.
     *
     * POST, e não PATCH, porque não é a edição de um campo: é a execução
     * de um processo que consulta um sistema externo, tem efeito colateral
     * em estoque e cujo resultado o chamador não escolhe. As três ações
     * abaixo seguem o mesmo critério, e é também o verbo que o client.js
     * do front já usa.
     */
    @PostMapping("/{id}/confirmar-pagamento")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PedidoResponse> confirmarPagamento(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoService.confirmarPagamento(id));
    }

    // RF0039 — despacho de compra aprovada (RN0039).
    @PostMapping("/{id}/despachar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PedidoResponse> despachar(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoService.despachar(id));
    }

    // RF0040 — confirmação de entrega (RN0040).
    @PostMapping("/{id}/confirmar-entrega")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PedidoResponse> confirmarEntrega(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoService.confirmarEntrega(id));
    }
}
