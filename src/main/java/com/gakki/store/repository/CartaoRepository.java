package com.gakki.store.repository;

import com.gakki.store.domain.Cartao;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CartaoRepository extends JpaRepository<Cartao, Long> {

    /**
     * A listagem exibe o nome da bandeira, que é um @ManyToOne LAZY.
     * Sem o EntityGraph, cinco cartões custariam seis consultas.
     */
    @EntityGraph(attributePaths = "bandeira")
    List<Cartao> findByClienteIdOrderByPreferencialDescIdAsc(Long clienteId);

    /** Mesmo motivo do endereço: o dono faz parte da chave de acesso. */
    @EntityGraph(attributePaths = "bandeira")
    Optional<Cartao> findByIdAndClienteId(Long id, Long clienteId);

    /** Candidato a assumir como preferencial quando o atual é removido (RF0027). */
    Optional<Cartao> findFirstByClienteIdAndIdNotOrderByIdAsc(Long clienteId, Long id);

    /** Limpa o preferencial atual antes de marcar outro — ux_cartao_preferencial não admite dois. */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Cartao c set c.preferencial = false where c.cliente.id = :clienteId and c.preferencial = true")
    void desmarcarPreferencial(@Param("clienteId") Long clienteId);
}
