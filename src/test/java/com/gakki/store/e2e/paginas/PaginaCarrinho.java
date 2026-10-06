package com.gakki.store.e2e.paginas;

import org.openqa.selenium.By;

// Carrinho (RF0032, RF0033). Visualizar, alterar quantidade, remover, e
// o botão que inicia a compra.
public final class PaginaCarrinho {

    public static final String CAMINHO = "/carrinho";

    public static final By ITENS = By.cssSelector("[data-testid='item-carrinho']");
    public static final By NOMES = By.cssSelector("[data-testid='item-nome']");
    public static final By SUBTOTAIS = By.cssSelector("[data-testid='item-subtotal']");
    public static final By BOTOES_REMOVER = By.cssSelector("[data-testid='btn-remover-item']");

    public static final By TOTAL = By.cssSelector("[data-testid='carrinho-total']");
    public static final By BOTAO_FINALIZAR = By.cssSelector("[data-testid='btn-finalizar-compra']");
    public static final By VAZIO = By.cssSelector("[data-testid='carrinho-vazio']");

    // Os campos de quantidade na ordem em que as linhas aparecem. Usado
    // com findElements + índice quando o teste não conhece o itemId.
    public static final By QUANTIDADES =
            By.cssSelector("[data-testid='item-carrinho'] .seletor-quantidade input");

    public static By itemDeId(long itemId) {
        return By.cssSelector("[data-testid='item-carrinho'][data-item-id='" + itemId + "']");
    }

    // O SeletorQuantidade do carrinho recebe id={`qtd-${itemId}`} — já
    // era seletor estável antes dos data-testid.
    public static By quantidadeDoItem(long itemId) {
        return By.id("qtd-" + itemId);
    }

    private PaginaCarrinho() {
    }
}
