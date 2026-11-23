package com.gakki.store.controller;

import com.gakki.store.dto.request.ConcluirTreinamentoRequest;
import com.gakki.store.dto.request.FalharTreinamentoRequest;
import com.gakki.store.dto.request.IniciarTreinamentoRequest;
import com.gakki.store.dto.response.ModeloIaResponse;
import com.gakki.store.dto.response.PaginaResponse;
import com.gakki.store.dto.response.PreviaTreinamentoResponse;
import com.gakki.store.service.TreinamentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;

// RNF0044, critério c — ajuste fino do modelo, operado pelo administrador.
@RestController
@RequestMapping("/ia/treinamentos")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class TreinamentoController {

    private static final MediaType JSONL = MediaType.parseMediaType("application/x-ndjson;charset=UTF-8");

    private final TreinamentoService treinamentoService;

    @GetMapping("/previa")
    public ResponseEntity<PreviaTreinamentoResponse> previa() {
        return ResponseEntity.ok(treinamentoService.previa());
    }

    @GetMapping
    public ResponseEntity<PaginaResponse<ModeloIaResponse>> listar(@PageableDefault(size = 20) Pageable paginacao) {
        return ResponseEntity.ok(treinamentoService.listar(paginacao));
    }

    @PostMapping
    public ResponseEntity<ModeloIaResponse> criar() {
        ModeloIaResponse modelo = treinamentoService.criar();
        URI localizacao = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/ia/treinamentos/{versao}/dados")
                .buildAndExpand(modelo.versao())
                .toUri();
        return ResponseEntity.created(localizacao).body(modelo);
    }

    // Arquivo que vai para o notebook do Colab, uma linha JSON por exemplo.
    @GetMapping("/{versao}/dados")
    public ResponseEntity<byte[]> dados(@PathVariable Integer versao) {
        byte[] conteudo = treinamentoService.arquivoDeTreino(versao).getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .contentType(JSONL)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"gakki-v" + versao + ".jsonl\"")
                .body(conteudo);
    }

    @PatchMapping("/{versao}/iniciar")
    public ResponseEntity<ModeloIaResponse> iniciar(@PathVariable Integer versao,
                                                    @Valid @RequestBody IniciarTreinamentoRequest requisicao) {
        return ResponseEntity.ok(treinamentoService.iniciar(versao, requisicao.jobId()));
    }

    @PatchMapping("/{versao}/concluir")
    public ResponseEntity<ModeloIaResponse> concluir(@PathVariable Integer versao,
                                                     @Valid @RequestBody ConcluirTreinamentoRequest requisicao) {
        return ResponseEntity.ok(treinamentoService.concluir(versao, requisicao.identificador()));
    }

    @PatchMapping("/{versao}/falhar")
    public ResponseEntity<ModeloIaResponse> falhar(@PathVariable Integer versao,
                                                   @Valid @RequestBody FalharTreinamentoRequest requisicao) {
        return ResponseEntity.ok(treinamentoService.falhar(versao, requisicao.motivo()));
    }

    @PatchMapping("/{versao}/ativar")
    public ResponseEntity<ModeloIaResponse> ativar(@PathVariable Integer versao) {
        return ResponseEntity.ok(treinamentoService.ativar(versao));
    }

    @PatchMapping("/{versao}/desativar")
    public ResponseEntity<ModeloIaResponse> desativar(@PathVariable Integer versao) {
        return ResponseEntity.ok(treinamentoService.desativar(versao));
    }
}
