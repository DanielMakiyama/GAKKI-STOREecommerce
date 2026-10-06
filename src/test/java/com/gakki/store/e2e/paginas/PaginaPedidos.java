package com.gakki.store.e2e.paginas;

import org.openqa.selenium.By;

// "Meus pedidos" (RF0038). Onde se prova que o status sobreviveu ao
// commit: a tela de confirmação mostra o pedido recém-criado em memória,
// esta relê do banco.
public final class PaginaPedidos {

    public static final String CAMINHO = "/pedidos";

    public static final By PEDIDOS = By.cssSelector("[data-testid='pedido']");
    public static final By SEM_PEDIDOS = By.cssSelector("[data-testid='sem-pedidos']");

    public static By pedidoDeNumero(String numero) {
        return By.cssSelector("[data-testid='pedido'][data-numero='" + numero + "']");
    }

    public static By totalDoPedido(String numero) {
        return By.cssSelector("[data-testid='pedido'][data-numero='" + numero + "']"
                + " [data-testid='pedido-total-lista']");
    }

    public static By cuponsDoPedido(String numero) {
        return By.cssSelector("[data-testid='pedido'][data-numero='" + numero + "']"
                + " [data-testid='pedido-cupons-lista']");
    }

    private PaginaPedidos() {
    }
}
