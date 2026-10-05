package com.gakki.store.controller;

import com.gakki.store.dto.response.InstrumentoResponse;
import com.gakki.store.dto.response.PaginaResponse;
import com.gakki.store.service.InstrumentoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// RF0011 — catálogo. Rotas públicas: a vitrine da loja é visível antes do
// login, como em qualquer e-commerce. Quem exige token é o carrinho.

@RestController
@RequestMapping("/instrumentos")
@RequiredArgsConstructor
public class InstrumentoController {

    private final InstrumentoService instrumentoService;

    @GetMapping
    public ResponseEntity<PaginaResponse<InstrumentoResponse>> listar(
            @PageableDefault(size = 20, sort = "nome", direction = Sort.Direction.ASC) Pageable paginacao) {

        return ResponseEntity.ok(instrumentoService.listar(paginacao));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InstrumentoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(instrumentoService.buscarPorId(id));
    }
}
