package com.gakki.store.domain.enums;

/**
 * Perfil de acesso do usuário.
 *
 * <p>Cada papel carrega a authority correspondente do Spring Security.
 * ADMINISTRADOR e GERENTE_VENDAS compartilham ROLE_ADMIN porque ambos
 * acessam o painel administrativo; a distinção entre eles só passa a
 * importar na autorização de margem de lucro (RN0014), fora do escopo
 * do módulo de clientes.
 */
public enum Papel {

    CLIENTE("ROLE_CLIENTE"),
    ADMINISTRADOR("ROLE_ADMIN"),
    GERENTE_VENDAS("ROLE_ADMIN");

    private final String authority;

    Papel(String authority) {
        this.authority = authority;
    }

    public String getAuthority() {
        return authority;
    }
}
