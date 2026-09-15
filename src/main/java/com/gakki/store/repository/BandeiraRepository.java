package com.gakki.store.repository;

import com.gakki.store.domain.Bandeira;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BandeiraRepository extends JpaRepository<Bandeira, Long> {

    List<Bandeira> findByAtivoTrueOrderByNomeAsc();

    /**
     * RN0025 — só bandeira registrada e ativa pode receber cartão novo.
     * A checagem do "ativa" vive na própria consulta: buscar por id e
     * conferir a flag depois abriria espaço para alguém esquecer o segundo passo.
     */
    Optional<Bandeira> findByIdAndAtivoTrue(Long id);
}
