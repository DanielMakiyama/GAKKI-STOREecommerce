package com.gakki.store.e2e.paginas;

import org.openqa.selenium.By;

/** Painel administrativo — aba de clientes (RF0023, RF0024). */
public final class PaginaAdmin {

    public static final String CAMINHO = "/admin";

    public static final By ABA_CLIENTES = By.cssSelector("[data-aba='clientes']");

    // Filtros da RF0024 — qualquer campo, isolado ou combinado.
    public static final By BUSCA = By.cssSelector("[data-testid='busca-cliente']");
    public static final By FILTRO_EMAIL = By.id("filtro-email");
    public static final By FILTRO_CPF = By.id("filtro-cpf");
    public static final By FILTRO_CODIGO = By.id("filtro-codigo");
    public static final By FILTRO_STATUS = By.id("filtro-status");
    public static final By BOTAO_FILTRAR = By.cssSelector("[data-testid='btn-filtrar']");
    public static final By BOTAO_LIMPAR = By.cssSelector("[data-testid='btn-limpar-filtros']");

    public static final By TOTAL = By.cssSelector("[data-testid='total-clientes']");
    public static final By LINHAS = By.cssSelector("tr[data-cliente]");

    private PaginaAdmin() {
    }

    /**
     * Linha do cliente pelo código do cadastro.
     *
     * <p>O código é o que o seed fixa (CLI-0002); o id depende da ordem
     * em que o banco foi populado e muda a cada recriação do schema.
     */
    public static By linhaComCodigo(String codigo) {
        return By.cssSelector("tr[data-codigo='" + codigo + "']");
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
