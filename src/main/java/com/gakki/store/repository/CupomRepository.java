package com.gakki.store.repository;

import com.gakki.store.domain.vendas.Cupom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface CupomRepository extends JpaRepository<Cupom, Long> {

    // Cupons que o cliente ainda pode usar, do maior para o menor.
    //
    // A ordem não é estética: o algoritmo da RN0036 trabalha sobre o
    // conjunto ordenado por valor para decidir se sobrou cupom
    // desnecessário (contrato de Vendas v3, decisão 4).
    List<Cupom> findByClienteIdAndUtilizadoFalseOrderByValorDesc(Long clienteId);

    // Resolve os códigos enviados no checkout.
    //
    // O clienteId entra na consulta junto com os códigos: um cupom de
    // outra pessoa não é "encontrado e recusado", ele simplesmente não
    // aparece. O service compara a quantidade devolvida com a pedida e
    // responde 400 se faltar algum — sem dizer se o código existe.
    List<Cupom> findByClienteIdAndCodigoIn(Long clienteId, Collection<String> codigos);

    // Usado na geração do cupom de troco (RN0036) para garantir que o
    // código novo não colide com um já emitido.
    boolean existsByCodigo(String codigo);
}
