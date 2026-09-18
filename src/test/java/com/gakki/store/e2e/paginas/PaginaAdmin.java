package com.gakki.store.e2e.paginas;

import org.openqa.selenium.By;

/** Painel administrativo — aba de clientes (RF0023, RF0024). */
public final class PaginaAdmin {

    public static final String CAMINHO = "/admin";

    public static final By ABA_CLIENTES = By.cssSelector("[data-aba='clientes']");
    public static final By BUSCA = By.cssSelector("[data-testid='busca-cliente']");

    private PaginaAdmin() {
    }

    /** Linha do cliente na tabela. Some da tela se o cadastro for excluído. */
    public static By linhaDoCliente(long clienteId) {
        return By.cssSelector("tr[data-cliente='" + clienteId + "']");
    }

    /** Selo de status dentro da linha — devolve "Ativo" ou "Inativo". */
    public static By statusDoCliente(long clienteId) {
        return By.cssSelector("tr[data-cliente='" + clienteId + "'] [data-status]");
    }

    /** Botão que alterna entre Inativar e Ativar, conforme o estado atual. */
    public static By botaoDeStatus(long clienteId) {
        return By.cssSelector("tr[data-cliente='" + clienteId + "'] [data-testid='btn-alternar-status']");
    }
}
