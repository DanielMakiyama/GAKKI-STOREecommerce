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

/**
 * Tradução de exceção para resposta HTTP, em formato ProblemDetail
 * (RFC 7807).
 *
 * <p>Estende {@link ResponseEntityExceptionHandler} para herdar o
 * tratamento das exceções que o próprio Spring MVC lança — JSON
 * malformado, método não suportado, parâmetro ausente — que de outra
 * forma escapariam para o handler genérico e virariam 500.
 */
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

    /**
     * Rede de segurança para as constraints do banco.
     *
     * <p>O service confere unicidade antes de gravar, mas entre a
     * consulta e o INSERT existe uma janela em que outra requisição pode
     * ter inserido o mesmo e-mail. Quando isso acontece, quem recusa é o
     * índice único — e a resposta precisa continuar sendo 409, não 500.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail tratarViolacaoDeIntegridade(DataIntegrityViolationException ex,
                                                     HttpServletRequest requisicao) {
        log.warn("Violação de integridade em {}: {}", requisicao.getRequestURI(), ex.getMostSpecificCause().getMessage());
        return montar(HttpStatus.CONFLICT, "Conflito com o estado atual",
                "A operação viola uma restrição de integridade dos dados.", requisicao);
    }

    /** Último recurso: nada de stack trace para o cliente, tudo no log do servidor. */
    @ExceptionHandler(Exception.class)
    public ProblemDetail tratarInesperado(Exception ex, HttpServletRequest requisicao) {
        log.error("Erro não tratado em {}", requisicao.getRequestURI(), ex);
        return montar(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno",
                "Ocorreu um erro inesperado. Tente novamente em alguns instantes.", requisicao);
    }

    /** Falhas de Bean Validation nos DTOs de entrada (RN0026, RNF0031, RN0023). */
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
