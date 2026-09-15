package com.gakki.store.repository;

import com.gakki.store.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /** Usado pela autenticação: o e-mail é o identificador de login. */
    Optional<Usuario> findByEmail(String email);

    /** Checagem de unicidade antes de gravar, para devolver 409 em vez de deixar estourar a constraint. */
    boolean existsByEmail(String email);
}
