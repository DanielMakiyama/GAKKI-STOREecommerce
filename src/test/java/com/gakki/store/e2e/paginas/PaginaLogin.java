package com.gakki.store.e2e.paginas;

import org.openqa.selenium.By;

//Tela de entrada — os dois cartões de perfil.

public final class PaginaLogin {

    public static final String CAMINHO = "/login";

    public static final By CARTAO_CLIENTE = By.cssSelector("[data-perfil='cliente']");
    public static final By CARTAO_ADMIN = By.cssSelector("[data-perfil='admin']");
    public static final By ERRO = By.cssSelector(".erro-form");

    private PaginaLogin() {
    }
}
