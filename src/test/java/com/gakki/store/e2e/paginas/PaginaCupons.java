package com.gakki.store.e2e.paginas;

import org.openqa.selenium.By;

// "Meus cupons" (RF0037). É a única tela onde o cupom de troco da RN0036
// pode ser visto: nenhuma tela de pedido exibe o troco emitido.
public final class PaginaCupons {

    public static final String CAMINHO = "/cupons";

    public static final By TABELA = By.cssSelector("[data-testid='tabela-cupons']");
    public static final By LINHAS = By.cssSelector("[data-testid='linha-cupom']");
    public static final By CODIGOS = By.cssSelector("[data-testid='cupom-codigo']");
    public static final By SEM_CUPONS = By.cssSelector("[data-testid='sem-cupons']");

    public static By linhaDoCupom(String codigo) {
        return By.cssSelector("[data-testid='linha-cupom'][data-codigo='" + codigo + "']");
    }

    public static By valorDoCupom(String codigo) {
        return By.cssSelector("[data-testid='linha-cupom'][data-codigo='" + codigo + "']"
                + " [data-testid='cupom-valor']");
    }

    public static By statusDoCupom(String codigo) {
        return By.cssSelector("[data-testid='linha-cupom'][data-codigo='" + codigo + "']"
                + " [data-testid='cupom-status']");
    }

    private PaginaCupons() {
    }
}
