package com.gakki.store.repository;

import com.gakki.store.domain.Endereco;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EnderecoRepository extends JpaRepository<Endereco, Long> {

    List<Endereco> findByClienteIdOrderByPrincipalDescIdAsc(Long clienteId);

    /**
     * Sempre buscar endereço por id <em>e</em> clienteId.
     *
     * <p>Com {@code findById} isolado, bastaria o cliente A chamar
     * {@code PUT /clientes/me/enderecos/7} para editar o endereço do
     * cliente B. O dono faz parte da chave de acesso, não de uma
     * validação posterior que alguém pode esquecer.
     */
    Optional<Endereco> findByIdAndClienteId(Long id, Long clienteId);

    /** RN0022 — todo cliente precisa manter ao menos um endereço de entrega. */
    long countByClienteIdAndEntregaTrue(Long clienteId);

    /** RN0021 — e ao menos um endereço de cobrança. */
    long countByClienteIdAndCobrancaTrue(Long clienteId);

    /** Candidato a assumir como principal quando o atual é removido. */
    Optional<Endereco> findFirstByClienteIdAndEntregaTrueAndIdNotOrderByIdAsc(Long clienteId, Long id);

    /**
     * Desmarca o principal atual antes de marcar outro.
     *
     * <p>O índice ux_endereco_principal não admite dois principais no
     * mesmo cliente. Se o novo fosse marcado antes de o antigo ser
     * limpo, o banco recusaria a operação no meio da transação — por
     * isso a limpeza vem primeiro, num UPDATE só.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Endereco e set e.principal = false where e.cliente.id = :clienteId and e.principal = true")
    void desmarcarPrincipal(@Param("clienteId") Long clienteId);
}
