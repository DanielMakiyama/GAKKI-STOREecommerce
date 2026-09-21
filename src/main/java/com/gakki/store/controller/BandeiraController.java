package com.gakki.store.controller;

import com.gakki.store.dto.response.BandeiraResponse;
import com.gakki.store.service.CartaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

//RN0025 — bandeiras registradas no sistema.

@RestController
@RequestMapping("/bandeiras")
@RequiredArgsConstructor
public class BandeiraController {

    private final CartaoService cartaoService;

    @GetMapping
    public ResponseEntity<List<BandeiraResponse>> listar() {
        return ResponseEntity.ok(cartaoService.listarBandeiras());
    }
}
