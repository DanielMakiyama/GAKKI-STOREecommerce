package com.gakki.store.repository;

import com.gakki.store.domain.vendas.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

// Categorias de instrumento (RN0012), tabela de domínio.

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    List<Categoria> findAllByOrderByNomeAsc();
    List<Categoria> findByNomeIn(Collection<String> nomes);
}
