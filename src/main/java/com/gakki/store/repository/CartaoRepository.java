package com.gakki.store.repository;

import com.gakki.store.domain.cliente.Cartao;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CartaoRepository extends JpaRepository<Cartao, Long> {


    @EntityGraph(attributePaths = "bandeira")
    List<Cartao> findByClienteIdOrderByPreferencialDescIdAsc(Long clienteId);

    @EntityGraph(attributePaths = "bandeira")
    Optional<Cartao> findByIdAndClienteId(Long id, Long clienteId);
    Optional<Cartao> findFirstByClienteIdAndIdNotOrderByIdAsc(Long clienteId, Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Cartao c set c.preferencial = false where c.cliente.id = :clienteId and c.preferencial = true")
    void desmarcarPreferencial(@Param("clienteId") Long clienteId);
}
