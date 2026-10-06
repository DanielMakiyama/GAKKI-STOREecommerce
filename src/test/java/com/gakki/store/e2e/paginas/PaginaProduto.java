package com.gakki.store.e2e.paginas;

import org.openqa.selenium.By;

// Página do produto. É aqui que o item entra no carrinho (RF0031) e aqui
// que a RN0031 aparece na interface.
public final class PaginaProduto {

    public static final By NOME = By.cssSelector("[data-testid='produto-nome']");
    public static final By PRECO = By.cssSelector("[data-testid='produto-preco']");
    public static final By ESTOQUE = By.cssSelector("[data-testid='produto-estoque']");

    // O SeletorQuantidade usa "quantidade" como id padrão; nesta tela ele
    // é montado sem `id`, então vale o padrão.
    public static final By QUANTIDADE = By.id("quantidade");

    public static final By BOTAO_ADICIONAR = By.cssSelector("[data-testid='btn-adicionar-carrinho']");

    // Confirmação de que a adição foi aceita pelo backend. Esperar por
    // ela, e não dormir, é o que torna o teste estável: o POST do
    // carrinho acontece entre o clique e o aviso.
    public static final By AVISO_ADICIONADO = By.cssSelector("[data-testid='aviso-adicionado']");

    // Onde o 400 da RN0031 chega à tela.
    public static final By ERRO = By.cssSelector("[data-testid='erro-produto']");

    public static String caminho(long instrumentoId) {
        return "/produtos/" + instrumentoId;
    }

    private PaginaProduto() {
    }
}
