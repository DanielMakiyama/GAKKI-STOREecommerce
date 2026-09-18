package com.gakki.store.controller;

import com.gakki.store.dto.request.AlterarSenhaRequest;
import com.gakki.store.dto.request.AtualizarClienteRequest;
import com.gakki.store.dto.response.CadastroAtualizadoResponse;
import com.gakki.store.dto.response.ClienteResponse;
import com.gakki.store.dto.response.ClienteResumoResponse;
import com.gakki.store.dto.response.PaginaResponse;
import com.gakki.store.security.UsuarioAutenticado;
import com.gakki.store.service.ClienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
     * RF0022 — o cliente altera os próprios dados cadastrais.
     *
     * <p>A resposta não é o cadastro puro: quando o e-mail muda, ela traz
     * junto uma sessão nova, porque o token anterior foi emitido para um
     * {@code subject} que deixou de existir.
     */
    @PutMapping("/me")
    public ResponseEntity<CadastroAtualizadoResponse> alterarMeuCadastro(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @Valid @RequestBody AtualizarClienteRequest requisicao) {

        return ResponseEntity.ok(clienteService.atualizar(usuario.getUsername(), requisicao));
    }

    /**
     * RF0028 — alteração isolada de senha.
     *
     * <p>Responde 204 sem corpo: não há nada de útil para devolver, e
     * ecoar qualquer coisa relacionada a senha é risco desnecessário.
     */
    @PatchMapping("/me/senha")
    public ResponseEntity<Void> alterarMinhaSenha(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @Valid @RequestBody AlterarSenhaRequest requisicao) {

        clienteService.alterarSenha(usuario.getUsername(), requisicao);
        return ResponseEntity.noContent().build();
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

    /**
     * RF0023 — o cliente encerra a própria conta.
     *
     * <p>Note que não existe {@code DELETE /clientes/me}. A ausência do
     * verbo é intencional: o cadastro não é excluído, e um endpoint
     * chamado DELETE sugeriria o contrário.
     */
    @PatchMapping("/me/inativar")
    public ResponseEntity<Void> inativarMinhaConta(
            @AuthenticationPrincipal UsuarioAutenticado usuario) {

        clienteService.inativarPropriaConta(usuario.getUsername());
        return ResponseEntity.noContent().build();
    }

    /** RF0023 — inativação pelo administrador. */
    @PatchMapping("/{id}/inativar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> inativar(@PathVariable Long id) {
        clienteService.inativar(id);
        return ResponseEntity.noContent().build();
    }

    /** RF0023 — reativação pelo administrador. */
    @PatchMapping("/{id}/ativar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> ativar(@PathVariable Long id) {
        clienteService.ativar(id);
        return ResponseEntity.noContent().build();
    }
}
