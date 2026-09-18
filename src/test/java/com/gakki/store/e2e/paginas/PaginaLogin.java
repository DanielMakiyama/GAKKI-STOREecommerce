package com.gakki.store.e2e.paginas;

import org.openqa.selenium.By;

/**
 * Tela de entrada — os dois cartões de perfil.
 *
 * <p>O seletor é {@code [data-perfil=...]}, e não o texto do botão: ele
 * vira "Entrando…" durante a requisição, e uma busca por texto
 * quebraria conforme a velocidade da rede.
 */
public final class PaginaLogin {

    public static final String CAMINHO = "/login";

    public static final By CARTAO_CLIENTE = By.cssSelector("[data-perfil='cliente']");
    public static final By CARTAO_ADMIN = By.cssSelector("[data-perfil='admin']");
    public static final By ERRO = By.cssSelector(".erro-form");

    private PaginaLogin() {
    }
}
