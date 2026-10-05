package com.gakki.store.controller;

import com.gakki.store.dto.request.AtualizarQuantidadeRequest;
import com.gakki.store.dto.request.ItemCarrinhoRequest;
import com.gakki.store.dto.response.CarrinhoResponse;
import com.gakki.store.dto.response.FreteResponse;
import com.gakki.store.security.UsuarioAutenticado;
import com.gakki.store.service.CarrinhoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// RF0031, RF0032 e RF0034 — carrinho do cliente autenticado.
//
// Não existe /carrinho/{id}: o carrinho é sempre o de quem está no token.
// Sem id na URL, não há como pedir o carrinho de outra pessoa.

@RestController
@RequestMapping("/carrinho")
@RequiredArgsConstructor
public class CarrinhoController {

    private final CarrinhoService carrinhoService;

    // RF0033 — "iniciar a compra" abre esta mesma leitura; o backend não
    // distingue ver o carrinho de ir para o checkout.
    @GetMapping
    public ResponseEntity<CarrinhoResponse> verCarrinho(
            @AuthenticationPrincipal UsuarioAutenticado usuario) {

        return ResponseEntity.ok(carrinhoService.verCarrinho(usuario.getUsername()));
    }

    // RF0031 — devolve o carrinho inteiro, não só o item criado: a tela
    // precisa do novo total e do contador do cabeçalho na mesma resposta.
    // Por isso 200, e não 201 com Location.
    @PostMapping("/itens")
    public ResponseEntity<CarrinhoResponse> adicionar(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @Valid @RequestBody ItemCarrinhoRequest requisicao) {

        return ResponseEntity.ok(carrinhoService.adicionar(usuario.getUsername(), requisicao));
    }

    // RF0032 — alteração da quantidade na visualização do carrinho.
    @PutMapping("/itens/{itemId}")
    public ResponseEntity<CarrinhoResponse> atualizarQuantidade(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @PathVariable Long itemId,
            @Valid @RequestBody AtualizarQuantidadeRequest requisicao) {

        return ResponseEntity.ok(carrinhoService.atualizarQuantidade(
                usuario.getUsername(), itemId, requisicao.quantidade()));
    }

    // RF0032 — exclusão de item. 204 sem corpo; a tela recarrega.
    @DeleteMapping("/itens/{itemId}")
    public ResponseEntity<Void> remover(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @PathVariable Long itemId) {

        carrinhoService.remover(usuario.getUsername(), itemId);
        return ResponseEntity.noContent().build();
    }

    // RF0034 — prévia do frete para o endereço escolhido no checkout.
    //
    // O endereço é parâmetro obrigatório porque a fórmula depende dele.
    // Este valor é só para a tela mostrar: o que entra no pedido é
    // recalculado no finalizarCompra (contrato de Vendas v3, decisão 14).
    @GetMapping("/frete")
    public ResponseEntity<FreteResponse> calcularFrete(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @RequestParam Long enderecoEntregaId) {

        return ResponseEntity.ok(carrinhoService.calcularFrete(
                usuario.getUsername(), enderecoEntregaId));
    }
}
