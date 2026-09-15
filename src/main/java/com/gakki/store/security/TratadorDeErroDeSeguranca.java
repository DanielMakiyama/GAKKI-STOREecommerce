package com.gakki.store.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.time.OffsetDateTime;

/**
 * Respostas 401 e 403 no mesmo formato ProblemDetail do resto da API.
 *
 * <p>Essas duas situações são barradas pelos filtros do Spring Security,
 * antes de qualquer controller existir — então o
 * {@code @RestControllerAdvice} nunca é acionado. Sem esta classe, o
 * cliente receberia a página de erro padrão do container em HTML, e o
 * front, que espera JSON, quebraria ao tentar interpretá-la.
 */
@Component
@RequiredArgsConstructor
public class TratadorDeErroDeSeguranca implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest requisicao,
                         HttpServletResponse resposta,
                         AuthenticationException excecao) throws IOException {
        escrever(requisicao, resposta, HttpStatus.UNAUTHORIZED, "Não autenticado",
                "Autenticação necessária para acessar este recurso.");
    }

    @Override
    public void handle(HttpServletRequest requisicao,
                       HttpServletResponse resposta,
                       AccessDeniedException excecao) throws IOException {
        escrever(requisicao, resposta, HttpStatus.FORBIDDEN, "Acesso negado",
                "Você não tem permissão para executar esta operação.");
    }

    private void escrever(HttpServletRequest requisicao,
                          HttpServletResponse resposta,
                          HttpStatus status,
                          String titulo,
                          String detalhe) throws IOException {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(status, detalhe);
        problema.setTitle(titulo);
        problema.setInstance(URI.create(requisicao.getRequestURI()));
        problema.setProperty("timestamp", OffsetDateTime.now());

        resposta.setStatus(status.value());
        resposta.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        resposta.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(resposta.getWriter(), problema);
    }
}
