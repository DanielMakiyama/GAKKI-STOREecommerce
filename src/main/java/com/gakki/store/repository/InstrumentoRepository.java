package com.gakki.store.repository;

import com.gakki.store.domain.vendas.Instrumento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InstrumentoRepository extends JpaRepository<Instrumento, Long> {

    Optional<Instrumento> findByIdAndAtivoTrue(Long id);

    @EntityGraph(attributePaths = {"fabricante", "categorias"})
    Page<Instrumento> findByAtivoTrue(Pageable paginacao);

    @EntityGraph(attributePaths = {"fabricante", "categorias"})
    Optional<Instrumento> findDetalhadoByIdAndAtivoTrue(Long id);
}
