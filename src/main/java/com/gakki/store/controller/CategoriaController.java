package com.gakki.store.controller;

import com.gakki.store.dto.response.CategoriaResponse;
import com.gakki.store.service.AnaliseVendasService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/*
 * RN0012 — categorias de instrumento cadastradas.
 *
 * Rota pública, como /bandeiras: é uma tabela de domínio, sem dado de
 * cliente, que serve para montar listas de seleção. Hoje quem usa é a
 * tela de análise de vendas (RF0055); o filtro do catálogo, que também é
 * público, pode usar a mesma.
 *
 * Fica num controller próprio, e não dentro de /analise, porque a
 * categoria é um recurso da loja, e não da análise. O service é o da
 * análise porque é quem já monta essa lista, do mesmo jeito que o
 * BandeiraController usa o CartaoService.
 */
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
