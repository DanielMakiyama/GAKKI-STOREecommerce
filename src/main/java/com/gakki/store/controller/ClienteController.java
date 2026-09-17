package com.gakki.store.controller;

import com.gakki.store.dto.response.ClienteResponse;
import com.gakki.store.dto.response.ClienteResumoResponse;
import com.gakki.store.dto.response.PaginaResponse;
import com.gakki.store.security.UsuarioAutenticado;
import com.gakki.store.service.ClienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;

    /**
     * RF0024 — o cliente consulta o próprio cadastro.
     *
     * <p>{@code @AuthenticationPrincipal} entrega o usuário que o filtro
     * JWT colocou no contexto. O identificador vem do token, não da URL:
     * sem parâmetro de id, não há como pedir o cadastro de outra pessoa.
     */
    @GetMapping("/me")
    public ResponseEntity<ClienteResponse> meuPerfil(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ResponseEntity.ok(clienteService.buscarPorEmail(usuario.getUsername()));
    }

    /**
     * RF0024 — consulta administrativa com filtros combinados.
     *
     * <p>Todos os parâmetros são opcionais e se combinam: a RF0024 exige
     * que qualquer campo sirva de filtro, isolado ou junto com os outros.
     *
     * <p>O {@code @PreAuthorize} fica aqui, ao lado do método que
     * protege, em vez de num padrão de URL no SecurityConfig. Quem lê o
     * endpoint vê a permissão exigida sem abrir outro arquivo.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaginaResponse<ClienteResumoResponse>> listar(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String cpf,
            @RequestParam(required = false) String codigo,
            @RequestParam(required = false) Boolean ativo,
            @PageableDefault(size = 20, sort = "nome", direction = Sort.Direction.ASC) Pageable paginacao) {

        return ResponseEntity.ok(
                clienteService.listar(nome, email, cpf, codigo, ativo, paginacao));
    }

    /** RF0024 — consulta administrativa de um cliente pelo id. */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClienteResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(clienteService.buscarPorId(id));
    }
}
