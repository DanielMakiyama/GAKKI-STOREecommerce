package com.gakki.store.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;


@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ProblemDetail tratarNaoEncontrado(RecursoNaoEncontradoException ex, HttpServletRequest requisicao) {
        return montar(HttpStatus.NOT_FOUND, "Recurso não encontrado", ex.getMessage(), requisicao);
    }

    @ExceptionHandler(ConflitoException.class)
    public ProblemDetail tratarConflito(ConflitoException ex, HttpServletRequest requisicao) {
        return montar(HttpStatus.CONFLICT, "Conflito com o estado atual", ex.getMessage(), requisicao);
    }

    @ExceptionHandler(RegraDeNegocioException.class)
    public ProblemDetail tratarRegraDeNegocio(RegraDeNegocioException ex, HttpServletRequest requisicao) {
        return montar(HttpStatus.BAD_REQUEST, "Regra de negócio violada", ex.getMessage(), requisicao);
    }

    @ExceptionHandler(CredencialInvalidaException.class)
    public ProblemDetail tratarCredencialInvalida(CredencialInvalidaException ex, HttpServletRequest requisicao) {
        return montar(HttpStatus.UNAUTHORIZED, "Credencial inválida", ex.getMessage(), requisicao);
    }

    @ExceptionHandler(ContaInativaException.class)
    public ProblemDetail tratarContaInativa(ContaInativaException ex, HttpServletRequest requisicao) {
        return montar(HttpStatus.FORBIDDEN, "Cadastro inativo", ex.getMessage(), requisicao);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail tratarAcessoNegado(AccessDeniedException ex, HttpServletRequest requisicao) {
        return montar(HttpStatus.FORBIDDEN, "Acesso negado",
                "Você não tem permissão para executar esta operação.", requisicao);
    }

    // RNF0044 — IA fora do ar; o chat não chega aqui, ele degrada sozinho.
    @ExceptionHandler(IaIndisponivelException.class)
    public ProblemDetail tratarIaIndisponivel(IaIndisponivelException ex, HttpServletRequest requisicao) {
        return montar(HttpStatus.SERVICE_UNAVAILABLE, "Assistente indisponível", ex.getMessage(), requisicao);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail tratarViolacaoDeIntegridade(DataIntegrityViolationException ex,
                                                     HttpServletRequest requisicao) {
        log.warn("Violação de integridade em {}: {}", requisicao.getRequestURI(), ex.getMostSpecificCause().getMessage());
        return montar(HttpStatus.CONFLICT, "Conflito com o estado atual",
                "A operação viola uma restrição de integridade dos dados.", requisicao);
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail tratarInesperado(Exception ex, HttpServletRequest requisicao) {
        log.error("Erro não tratado em {}", requisicao.getRequestURI(), ex);
        return montar(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno",
                "Ocorreu um erro inesperado. Tente novamente em alguns instantes.", requisicao);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders cabecalhos,
                                                                  HttpStatusCode status,
                                                                  WebRequest requisicao) {
        List<ErroDeCampo> erros = ex.getBindingResult().getFieldErrors().stream()
                .map(erro -> new ErroDeCampo(erro.getField(), erro.getDefaultMessage()))
                .sorted(Comparator.comparing(ErroDeCampo::campo))
                .toList();

        ProblemDetail problema = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "A requisição contém campos inválidos.");
        problema.setTitle("Dados inválidos");
        problema.setInstance(uriDe(requisicao));
        problema.setProperty("timestamp", OffsetDateTime.now());
        problema.setProperty("errors", erros);

        return ResponseEntity.badRequest().body(problema);
    }

    private ProblemDetail montar(HttpStatus status, String titulo, String detalhe, HttpServletRequest requisicao) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(status, detalhe);
        problema.setTitle(titulo);
        problema.setInstance(URI.create(requisicao.getRequestURI()));
        problema.setProperty("timestamp", OffsetDateTime.now());
        return problema;
    }

    private URI uriDe(WebRequest requisicao) {
        if (requisicao instanceof ServletWebRequest servletRequest) {
            return URI.create(servletRequest.getRequest().getRequestURI());
        }
        return URI.create(requisicao.getDescription(false));
    }
}
