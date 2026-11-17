package com.gakki.store.controller;

import com.gakki.store.dto.response.HistoricoVendasResponse;
import com.gakki.store.service.AnaliseVendasService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

//RF0055 - análise do histórico de vendas por categoria.

@RestController
@RequestMapping("/analise")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AnaliseVendasController {

    private final AnaliseVendasService analiseVendasService;

    @GetMapping("/vendas")
    public ResponseEntity<List<HistoricoVendasResponse>> historicoDeVendas(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam(required = false) List<String> categorias) {

        return ResponseEntity.ok(analiseVendasService.historico(inicio, fim, categorias));
    }
}
