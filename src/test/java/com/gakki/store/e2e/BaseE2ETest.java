package com.gakki.store.e2e;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Base dos testes de aceitação pela interface.
 *
 * <p>Marcada com {@code @Tag("e2e")}, que o surefire exclui do
 * {@code mvn test} comum — estes testes abrem um navegador e exigem
 * backend, banco e front no ar. Rodam com {@code mvn test -Pe2e}.
 *
 * <p>Pré-requisitos, nesta ordem:
 * <ol>
 *   <li>PostgreSQL rodando com o banco {@code gakki} migrado</li>
 *   <li>Backend em {@code localhost:8080} (JWT_SECRET definido)</li>
 *   <li>Front em {@code localhost:5173} ({@code npm run dev})</li>
 * </ol>
 *
 * <p>Parâmetros: {@code -De2e.url=...} muda a URL do front e
 * {@code -De2e.headless=true} roda sem abrir janela. Em apresentação,
 * deixe visível — ver o navegador se preencher sozinho é metade do
 * argumento.
 */
@Tag("e2e")
public abstract class BaseE2ETest {

    protected static final String URL_BASE = System.getProperty("e2e.url", "http://localhost:5173");
    private static final Duration TEMPO_LIMITE = Duration.ofSeconds(15);

    protected WebDriver navegador;
    protected WebDriverWait espera;

    @BeforeEach
    void abrirNavegador() {
        ChromeOptions opcoes = new ChromeOptions();
        opcoes.addArguments("--window-size=1440,1000");
        if (Boolean.parseBoolean(System.getProperty("e2e.headless", "false"))) {
            opcoes.addArguments("--headless=new");
        }

        // Sem WebDriverManager: o Selenium Manager resolve o ChromeDriver
        // compatível com o Chrome instalado, sozinho.
        navegador = new ChromeDriver(opcoes);
        espera = new WebDriverWait(navegador, TEMPO_LIMITE);
    }

    @AfterEach
    void fecharNavegador() {
        if (navegador != null) {
            navegador.quit();
        }
    }

    protected void abrir(String caminho) {
        navegador.get(URL_BASE + caminho);
    }

    /**
     * Espera o elemento aparecer antes de devolvê-lo.
     *
     * <p>Toda busca passa por aqui de propósito. A tela é uma SPA: o
     * conteúdo chega depois de um fetch, então um {@code findElement}
     * direto encontra a página ainda vazia e falha de forma
     * intermitente — passa numa máquina rápida, falha na do professor.
     */
    protected WebElement esperarPor(By seletor) {
        return espera.until(ExpectedConditions.visibilityOfElementLocated(seletor));
    }

    protected void clicar(By seletor) {
        espera.until(ExpectedConditions.elementToBeClickable(seletor)).click();
    }

    protected void preencher(By seletor, String valor) {
        WebElement campo = esperarPor(seletor);
        campo.clear();
        campo.sendKeys(valor);
    }

    protected String textoDe(By seletor) {
        return esperarPor(seletor).getText();
    }

    protected void esperarUrlConter(String trecho) {
        espera.until(ExpectedConditions.urlContains(trecho));
    }
}
