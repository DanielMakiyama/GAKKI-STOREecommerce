package com.gakki.store.e2e.paginas;

import org.openqa.selenium.By;

// Vitrine (RF0011). Ponto de partida de todo cenário de venda: o
// enunciado exige que o item entre no carrinho pela tela.
//
// O card NÃO tem botão de adicionar — só o link do título — então passar
// pela página do produto não é rodeio, é o único caminho que existe.
public final class PaginaCatalogo {

    public static final String CAMINHO = "/catalogo";

    public static final By GRADE = By.cssSelector("[data-testid='grade-produtos']");
    public static final By CARDS = By.cssSelector("[data-testid='produto-card']");
    public static final By PRECOS = By.cssSelector("[data-testid='produto-card-preco']");

    // Já existia antes dos data-testid, por ser um <select> com id próprio.
    public static final By ORDENACAO = By.id("ordenacao-catalogo");

    // O card carrega o id do instrumento, não o código: o teste resolve
    // código -> id por ApiDeApoio.idDoInstrumento e chega aqui com o id.
    // Fixar o id no código do teste quebraria a cada recriação do schema.
    public static By cardDoProduto(long instrumentoId) {
        return By.cssSelector("[data-testid='produto-card'][data-produto-id='" + instrumentoId + "']");
    }

    public static By linkDoProduto(long instrumentoId) {
        return By.cssSelector("[data-testid='produto-card'][data-produto-id='" + instrumentoId + "']"
                + " [data-testid='produto-card-link']");
    }

    private PaginaCatalogo() {
    }
}
