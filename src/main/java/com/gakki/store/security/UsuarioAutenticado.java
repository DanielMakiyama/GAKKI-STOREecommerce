package com.gakki.store.security;

import com.gakki.store.domain.Usuario;
import com.gakki.store.domain.enums.Papel;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Adapta {@link Usuario} ao contrato do Spring Security.
 *
 * <p>Existe para o domínio não precisar implementar {@code UserDetails}:
 * a entidade continua sendo só dado, e a dependência do framework fica
 * confinada no pacote de segurança.
 */
public class UsuarioAutenticado implements UserDetails {

    private final Long id;
    private final String email;
    private final String senha;
    private final Papel papel;
    private final boolean ativo;

    public UsuarioAutenticado(Usuario usuario) {
        this.id = usuario.getId();
        this.email = usuario.getEmail();
        this.senha = usuario.getSenha();
        this.papel = usuario.getPapel();
        this.ativo = usuario.isAtivo();
    }

    public Long getId() {
        return id;
    }

    public Papel getPapel() {
        return papel;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(papel.getAuthority()));
    }

    @Override
    public String getPassword() {
        return senha;
    }

    @Override
    public String getUsername() {
        return email;
    }

    /**
     * RF0023 — cadastro inativado não autentica.
     *
     * <p>Devolver {@code false} aqui faz o próprio Spring Security
     * recusar o login com DisabledException. A regra não depende de o
     * AuthService lembrar de checar a flag.
     */
    @Override
    public boolean isEnabled() {
        return ativo;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
}
