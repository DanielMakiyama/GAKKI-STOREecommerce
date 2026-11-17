package com.gakki.store.repository;

import com.gakki.store.domain.cliente.Endereco;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EnderecoRepository extends JpaRepository<Endereco, Long> {

    List<Endereco> findByClienteIdOrderByPrincipalDescIdAsc(Long clienteId);

    Optional<Endereco> findByIdAndClienteId(Long id, Long clienteId);

    long countByClienteIdAndEntregaTrue(Long clienteId);

    long countByClienteIdAndCobrancaTrue(Long clienteId);

    Optional<Endereco> findFirstByClienteIdAndEntregaTrueAndIdNotOrderByIdAsc(Long clienteId, Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Endereco e set e.principal = false where e.cliente.id = :clienteId and e.principal = true")
    void desmarcarPrincipal(@Param("clienteId") Long clienteId);
}
