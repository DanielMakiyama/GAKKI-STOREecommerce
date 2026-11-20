package com.gakki.store.repository;

import com.gakki.store.domain.recomendacao.PreferenciaCliente;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PreferenciaClienteRepository extends JpaRepository<PreferenciaCliente, Long> {

    // Categorias e estilos vêm na mesma consulta: o perfil e o chat usam os dois.
    @EntityGraph(attributePaths = {"categorias", "estilos"})
    Optional<PreferenciaCliente> findByClienteId(Long clienteId);
}
