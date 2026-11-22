package com.gakki.store.controller;

import com.gakki.store.dto.request.SimulacaoIaRequest;
import com.gakki.store.dto.response.IaStatusResponse;
import com.gakki.store.dto.response.SimulacaoIaResponse;
import com.gakki.store.service.AssistenteIaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// RNF0044 — integração com a IA, vista pelo administrador.
@RestController
@RequestMapping("/ia")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class IaController {

    private final AssistenteIaService assistenteIaService;

    @GetMapping("/status")
    public ResponseEntity<IaStatusResponse> status() {
        return ResponseEntity.ok(assistenteIaService.status());
    }

    // 200 e não 201: nada é gravado, é só uma consulta à IA.
    @PostMapping("/simulacoes")
    public ResponseEntity<SimulacaoIaResponse> simular(@Valid @RequestBody SimulacaoIaRequest requisicao) {
        return ResponseEntity.ok(assistenteIaService.simular(requisicao));
    }
}
