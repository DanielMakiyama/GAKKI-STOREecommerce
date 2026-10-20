package com.gakki.store.controller;

import com.gakki.store.dto.response.NotificacaoResponse;
import com.gakki.store.dto.response.PaginaResponse;
import com.gakki.store.security.UsuarioAutenticado;
import com.gakki.store.service.NotificacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/*
 * RN0046 — as notificações do cliente.
 *
 * Só leitura. A regra manda o sistema gerar a notificação; quem a gera é
 * o fluxo de troca, e marcar como lida não é comportamento que nenhum
 * requisito descreva.
 */
@RestController
@RequestMapping("/notificacoes")
@RequiredArgsConstructor
public class NotificacaoController {

    private final NotificacaoService notificacaoService;

    @GetMapping
    public ResponseEntity<PaginaResponse<NotificacaoResponse>> minhasNotificacoes(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @PageableDefault(size = 20) Pageable paginacao) {

        return ResponseEntity.ok(
                notificacaoService.minhasNotificacoes(usuario.getUsername(), paginacao));
    }
}
