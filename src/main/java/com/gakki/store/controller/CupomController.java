package com.gakki.store.controller;

import com.gakki.store.dto.response.CupomResponse;
import com.gakki.store.security.UsuarioAutenticado;
import com.gakki.store.service.CupomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// RF0037 - cupons do cliente autenticado.
@RestController
@RequestMapping("/cupons")
@RequiredArgsConstructor
public class CupomController {

    private final CupomService cupomService;

    @GetMapping
    public ResponseEntity<List<CupomResponse>> meusCupons(
            @AuthenticationPrincipal UsuarioAutenticado usuario) {

        return ResponseEntity.ok(cupomService.listarMeusCupons(usuario.getUsername()));
    }
}
