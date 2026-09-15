package com.gakki.store.repository;

import com.gakki.store.domain.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ClienteRepository
        extends JpaRepository<Cliente, Long>, JpaSpecificationExecutor<Cliente> {

    /**
     * Busca o cliente a partir do e-mail que veio no token.
     *
     * <p>O {@code @EntityGraph} traz o usuário na mesma consulta. Sem ele,
     * como o relacionamento é LAZY, ler {@code cliente.getUsuario().getEmail()}
     * dispararia um SELECT extra — e com {@code open-in-view: false} isso
     * viraria LazyInitializationException fora da transação.
     */
    @EntityGraph(attributePaths = "usuario")
    Optional<Cliente> findByUsuarioEmail(String email);

    @EntityGraph(attributePaths = "usuario")
    Optional<Cliente> findWithUsuarioById(Long id);

    /** RN0026 — CPF é único no sistema. */
    boolean existsByCpf(String cpf);

    boolean existsByCpfAndIdNot(String cpf, Long id);

    /**
     * RF0024 — consulta por qualquer combinação de campos.
     *
     * <p>Sobrescrito apenas para acrescentar o {@code @EntityGraph}: a
     * listagem exibe e-mail e status, que moram em {@code usuario}. Sem
     * isso seriam N consultas extras para uma página de N clientes.
     */
    @Override
    @EntityGraph(attributePaths = "usuario")
    Page<Cliente> findAll(Specification<Cliente> spec, Pageable pageable);

    /**
     * RNF0035 — código único de cliente.
     *
     * <p>Vem de uma sequence do PostgreSQL, e não de um contador em
     * memória: contador na aplicação reinicia junto com ela e não
     * sobrevive a duas instâncias rodando ao mesmo tempo.
     */
    @Query(value = "SELECT nextval('seq_codigo_cliente')", nativeQuery = true)
    Long proximoNumeroDeCodigo();
}
