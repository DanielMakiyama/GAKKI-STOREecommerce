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

// RF0037 — cupons do cliente autenticado.
//
// Só leitura: não existe cadastro de cupom nesta fase. Os promocionais
// vêm do seed e os de troca são emitidos pelo troco do pagamento
// (RN0036), nunca por requisição do cliente — se existisse POST aqui,
// qualquer um emitiria dinheiro para si mesmo.
//
// Lista sem paginação, ao contrário das outras: a quantidade é limitada
// pela natureza do dado — cupons de um cliente só, e já consumidos saem
// de circulação. Se um dia virar promoção em massa, vira Pageable.

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
