package com.gakki.store.repository;

import com.gakki.store.domain.vendas.Instrumento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InstrumentoRepository extends JpaRepository<Instrumento, Long> {

    // Só instrumento ativo pode entrar no carrinho (RN0031). A condição
    // está na consulta, e não num if depois de carregar: assim um produto
    // inativado nunca chega nem a ser lido.
    Optional<Instrumento> findByIdAndAtivoTrue(Long id);

    // Listagem do catálogo, paginada como toda listagem do projeto.
    // EntityGraph nas categorias: sem ele, uma página de 20 instrumentos
    // custaria 21 consultas para montar os filtros de categoria.
    @EntityGraph(attributePaths = "categorias")
    Page<Instrumento> findByAtivoTrue(Pageable paginacao);
}
