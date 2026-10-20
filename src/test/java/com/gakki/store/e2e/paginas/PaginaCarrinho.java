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

    // ------------------------------------------------------------
    // Bloqueio e expiração (RN0044, RN0045, RNF0042)
    // ------------------------------------------------------------

    /** Contagem do prazo. Só existe enquanto há contagem em curso. */
    public static final By CONTAGEM = By.cssSelector("[data-testid='contagem-bloqueio']");
    public static final By CONTAGEM_VALOR = By.cssSelector("[data-testid='contagem-valor']");

    /** Bloco dos itens retirados por expiração (RNF0042). */
    public static final By BLOCO_RETIRADOS = By.cssSelector("[data-testid='itens-retirados']");
    public static final By AVISO_RETIRADOS = By.cssSelector("[data-testid='aviso-itens-retirados']");
    public static final By ITENS_RETIRADOS = By.cssSelector("[data-testid='item-retirado']");
    public static final By NOMES_RETIRADOS = By.cssSelector("[data-testid='item-retirado-nome']");
    public static final By BOTOES_RESTAURAR = By.cssSelector("[data-testid='btn-restaurar-item']");
    public static final By BOTOES_DESCARTAR = By.cssSelector("[data-testid='btn-descartar-item']");
    public static final By BOTAO_DESCARTAR_TODOS = By.cssSelector("[data-testid='btn-descartar-todos']");

    /** Explicação de por que a compra está desabilitada (RNF0042). */
    public static final By MOTIVO_BLOQUEIO = By.cssSelector("[data-testid='motivo-compra-bloqueada']");

    /** Mensagem de erro do carrinho — recusa de disponibilidade, por exemplo. */
    public static final By ERRO = By.cssSelector("[data-testid='erro-carrinho']");

    /** Carrinho sem item ativo, mas com itens retirados: não é o vazio. */
    public static final By SEM_ITENS_ATIVOS =
            By.cssSelector("[data-testid='carrinho-sem-itens-ativos']");

    private PaginaCarrinho() {
    }
}
