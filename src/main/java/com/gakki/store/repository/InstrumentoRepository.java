package com.gakki.store.repository;

import com.gakki.store.domain.vendas.Instrumento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InstrumentoRepository extends JpaRepository<Instrumento, Long> {

    // Usado ao adicionar ao carrinho. Sem EntityGraph de propósito: ali só
    // interessam nome, preço e estoque, que são colunas da própria tabela.
    //
    // Só instrumento ativo entra no carrinho (RN0031). A condição está na
    // consulta, e não num if depois de carregar: um produto inativado
    // nunca chega nem a ser lido.
    Optional<Instrumento> findByIdAndAtivoTrue(Long id);

    // Usado pelo catálogo, que imprime fabricante e categorias.
    //
    // As duas relações entram no EntityGraph porque o DTO as lê: sem isso,
    // uma página de 20 instrumentos custaria 41 consultas — uma da página,
    // 20 dos fabricantes e 20 das categorias. É o N+1 clássico, e com
    // open-in-view: false nem chegaria a ser lento, estouraria
    // LazyInitializationException ao montar a resposta.
    //
    // Fabricante é @ManyToOne (join simples) e categorias é @ManyToMany
    // (coleção), então o Hibernate resolve cada uma com uma estratégia
    // diferente — não é o caso do MultipleBagFetchException, que exige
    // duas coleções.
    @EntityGraph(attributePaths = {"fabricante", "categorias"})
    Page<Instrumento> findByAtivoTrue(Pageable paginacao);

    @EntityGraph(attributePaths = {"fabricante", "categorias"})
    Optional<Instrumento> findDetalhadoByIdAndAtivoTrue(Long id);
}
