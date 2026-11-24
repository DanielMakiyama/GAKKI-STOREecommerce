package com.gakki.store.e2e.paginas;

import org.openqa.selenium.By;

// Cartão "Preferências musicais" do perfil (RNF0044), no PreferenciasCliente.jsx.
public final class PaginaPreferencias {

    public static final By FORMULARIO = By.cssSelector("[data-testid='form-preferencias']");
    public static final By NIVEL = By.id("pref-nivel");
    public static final By ORCAMENTO = By.id("pref-orcamento");
    public static final By OBSERVACOES = By.id("pref-observacoes");
    public static final By BOTAO_SALVAR = By.cssSelector("[data-testid='btn-salvar-preferencias']");
    public static final By SUCESSO = By.cssSelector("[data-testid='sucesso-preferencias']");
    public static final By ERRO = By.cssSelector("[data-testid='erro-preferencias']");

    // A categoria é achada pelo nome, e não pelo id, que muda a cada recriação do banco.
    public static By categoria(String nome) {
        return By.xpath("//*[@data-testid='pref-categorias']/button[normalize-space()='" + nome + "']");
    }

    public static By estilo(String valor) {
        return By.cssSelector("[data-testid='pref-estilo-" + valor + "']");
    }

    private PaginaPreferencias() {
    }
}
