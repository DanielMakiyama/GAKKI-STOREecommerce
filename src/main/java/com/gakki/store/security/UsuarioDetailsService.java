package com.gakki.store.security;

import com.gakki.store.domain.cliente.Usuario;
import com.gakki.store.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email.toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new UsernameNotFoundException("Credenciais inválidas"));
        return new UsuarioAutenticado(usuario);
    }
}
