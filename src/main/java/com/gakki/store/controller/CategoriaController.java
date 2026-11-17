package com.gakki.store.controller;

import com.gakki.store.dto.response.CategoriaResponse;
import com.gakki.store.service.AnaliseVendasService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// RN0012 - categorias de instrumento cadastradas.

@RestController
@RequestMapping("/categorias")
@RequiredArgsConstructor
public class CategoriaController {

    private final AnaliseVendasService analiseVendasService;

    // Sem paginação, como /bandeiras: lista pequena e fechada.
    @GetMapping
    public ResponseEntity<List<CategoriaResponse>> listar() {
        return ResponseEntity.ok(analiseVendasService.listarCategorias());
    }
}
