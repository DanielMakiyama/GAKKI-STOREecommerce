package com.gakki.store.repository;

import com.gakki.store.domain.recomendacao.ModeloIa;
import com.gakki.store.domain.recomendacao.enums.StatusModeloIa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ModeloIaRepository extends JpaRepository<ModeloIa, Long> {

    // A versão que atende o chat. O índice ux_modelo_ia_ativo garante no máximo uma.
    Optional<ModeloIa> findByAtivoTrue();

    Optional<ModeloIa> findTopByOrderByVersaoDesc();

    // Treinamentos em andamento, consultados periodicamente no provedor.
    List<ModeloIa> findByStatus(StatusModeloIa status);

    Page<ModeloIa> findAllByOrderByVersaoDesc(Pageable paginacao);
}
