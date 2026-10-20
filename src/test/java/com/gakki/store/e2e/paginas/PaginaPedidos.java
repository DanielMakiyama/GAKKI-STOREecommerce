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

    public static By statusDoPedido(String numero) {
        return By.cssSelector("[data-testid='pedido'][data-numero='" + numero + "']"
                + " [data-testid='pedido-status-lista']");
    }

    // ------------------------------------------------------------
    // Solicitação de troca (RF0041, RN0041)
    //
    // O painel só aparece em pedido ENTREGUE — é a própria RN0043
    // visível na tela.
    // ------------------------------------------------------------

    public static final By MENSAGEM = By.cssSelector("[data-testid='mensagem-pedidos']");

    public static By painelDeTroca(String numero) {
        return By.cssSelector("[data-testid='pedido'][data-numero='" + numero + "']"
                + " [data-testid='painel-troca']");
    }

    public static By caixaDoItem(String numero, long itemPedidoId) {
        return By.cssSelector("[data-testid='pedido'][data-numero='" + numero + "']"
                + " [data-testid='check-item-troca'][data-item-pedido-id='" + itemPedidoId + "']");
    }

    public static By botaoSolicitarTroca(String numero) {
        return By.cssSelector("[data-testid='pedido'][data-numero='" + numero + "']"
                + " [data-testid='btn-solicitar-troca']");
    }

    private PaginaPedidos() {
    }
}
