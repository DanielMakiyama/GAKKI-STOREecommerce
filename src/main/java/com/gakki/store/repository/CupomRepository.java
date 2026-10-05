package com.gakki.store.repository;

import com.gakki.store.domain.vendas.Cupom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface CupomRepository extends JpaRepository<Cupom, Long> {

    // Todos os cupons do cliente, utilizados ou não — a tela "Meus
    // cupons" mostra os dois estados, com a etiqueta Utilizado/Disponível.
    List<Cupom> findByClienteIdOrderByUtilizadoAscValorDesc(Long clienteId);

    // Resolve os códigos enviados no checkout.
    //
    // O clienteId entra na consulta junto com os códigos: um cupom de
    // outra pessoa não é "encontrado e recusado", ele simplesmente não
    // aparece. O service compara a quantidade devolvida com a pedida e
    // responde 400 se faltar algum — sem dizer se o código existe.
    List<Cupom> findByClienteIdAndCodigoIn(Long clienteId, Collection<String> codigos);

    boolean existsByCodigo(String codigo);

    // RN0036 — numeração do cupom de troco, pela sequence da V14.
    @Query(value = "SELECT nextval('seq_codigo_cupom')", nativeQuery = true)
    Long proximoNumeroDeCupom();
}
