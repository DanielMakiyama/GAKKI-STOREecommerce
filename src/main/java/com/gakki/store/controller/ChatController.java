package com.gakki.store.controller;

import com.gakki.store.dto.request.AvaliacaoRespostaRequest;
import com.gakki.store.dto.request.MensagemChatRequest;
import com.gakki.store.dto.response.ConversaResponse;
import com.gakki.store.dto.response.MensagemChatResponse;
import com.gakki.store.dto.response.PaginaResponse;
import com.gakki.store.dto.response.RespostaChatResponse;
import com.gakki.store.security.UsuarioAutenticado;
import com.gakki.store.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

// RNF0044 — chatbot de recomendação, usado só pelo cliente (critério a).
@RestController
@RequestMapping("/chat")
@PreAuthorize("hasRole('CLIENTE')")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    // 204 quando o cliente ainda não tem conversa.
    @GetMapping("/conversas/atual")
    public ResponseEntity<ConversaResponse> conversaAtual(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return chatService.conversaAtual(usuario.getUsername())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PostMapping("/conversas")
    public ResponseEntity<ConversaResponse> novaConversa(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        ConversaResponse conversa = chatService.novaConversa(usuario.getUsername());
        return ResponseEntity.created(mensagensDa(conversa.id())).body(conversa);
    }

    @GetMapping("/conversas/{id}/mensagens")
    public ResponseEntity<PaginaResponse<MensagemChatResponse>> mensagens(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @PathVariable Long id,
            @PageableDefault(size = 30) Pageable paginacao) {

        return ResponseEntity.ok(chatService.mensagens(usuario.getUsername(), id, paginacao));
    }

    // Pergunta e resposta são gravadas: 201. Se a IA falhar, a resposta vem nula com um aviso.
    @PostMapping("/conversas/{id}/mensagens")
    public ResponseEntity<RespostaChatResponse> enviar(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @PathVariable Long id,
            @Valid @RequestBody MensagemChatRequest requisicao) {

        RespostaChatResponse resposta = chatService.enviar(usuario.getUsername(), id, requisicao.texto());
        return ResponseEntity.created(mensagensDa(id)).body(resposta);
    }

    // PUT: avaliar de novo substitui a avaliação anterior.
    @PutMapping("/mensagens/{id}/avaliacao")
    public ResponseEntity<MensagemChatResponse> avaliar(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @PathVariable Long id,
            @Valid @RequestBody AvaliacaoRespostaRequest requisicao) {

        return ResponseEntity.ok(chatService.avaliar(usuario.getUsername(), id, requisicao.avaliacao()));
    }

    private URI mensagensDa(Long conversaId) {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/chat/conversas/{id}/mensagens")
                .buildAndExpand(conversaId)
                .toUri();
    }
}
