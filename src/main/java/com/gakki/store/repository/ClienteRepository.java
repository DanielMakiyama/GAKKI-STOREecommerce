package com.gakki.store.repository;

import com.gakki.store.domain.cliente.Cliente;
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

    // Busca o cliente a partir do e-mail que veio no token.
    @EntityGraph(attributePaths = "usuario")
    Optional<Cliente> findByUsuarioEmail(String email);

    @EntityGraph(attributePaths = "usuario")
    Optional<Cliente> findWithUsuarioById(Long id);

    // RN0026 — CPF é único no sistema.
    boolean existsByCpf(String cpf);

    boolean existsByCpfAndIdNot(String cpf, Long id);

    // RF0024 — consulta por qualquer combinação de campos.
    @Override
    @EntityGraph(attributePaths = "usuario")
    Page<Cliente> findAll(Specification<Cliente> spec, Pageable pageable);

    // RNF0035 — código único de cliente.
    @Query(value = "SELECT nextval('seq_codigo_cliente')", nativeQuery = true)
    Long proximoNumeroDeCodigo();
}
