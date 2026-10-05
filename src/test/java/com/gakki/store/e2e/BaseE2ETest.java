package com.gakki.store.e2e;

import com.gakki.store.e2e.acoes.FluxoDeCadastro;
import com.gakki.store.e2e.paginas.PaginaAdmin;
import com.gakki.store.e2e.paginas.PaginaLogin;
import com.gakki.store.e2e.paginas.PaginaPerfil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

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

    protected void selecionarPorValor(By seletor, String valor) {
        new Select(esperarPor(seletor)).selectByValue(valor);
    }

    protected void selecionarPorIndice(By seletor, int indice) {
        new Select(esperarPor(seletor)).selectByIndex(indice);
    }

    protected void marcarCaixa(By seletor, boolean marcada) {
        WebElement caixa = esperarPor(seletor);
        if (caixa.isSelected() != marcada) {
            caixa.click();
        }
    }

    /** Valor de um atributo HTML — usado para ler os {@code data-*}. */
    protected String atributoDe(By seletor, String atributo) {
        return esperarPor(seletor).getDomAttribute(atributo);
    }

    protected void esperarQuantidade(By seletor, int quantidade) {
        espera.until(ExpectedConditions.numberOfElementsToBe(seletor, quantidade));
    }

    protected void substituir(By seletor, String valor) {
        esperarPor(seletor).sendKeys(Keys.chord(Keys.CONTROL, "a"), valor);
    }

    protected void esperarUrlConter(String trecho) {
        espera.until(ExpectedConditions.urlContains(trecho));
    }

    /** Valor atual de um campo de formulário. */
    protected String valorDe(By seletor) {
        // getDomProperty, e não getDomAttribute: o atributo HTML guarda o
        // valor inicial, e num campo controlado pelo React ele não
        // acompanha o que o usuário digitou nem o que o state trouxe.
        return esperarPor(seletor).getDomProperty("value");
    }

    protected void esperarCampoPreenchido(By seletor) {
        espera.until(nav -> {
            String valor = nav.findElement(seletor).getDomProperty("value");
            return valor != null && !valor.isBlank();
        });
    }

    // Fluxo de cadastro ligado ao navegador desta execução
    protected FluxoDeCadastro cadastro() {
        return new FluxoDeCadastro(navegador, espera, URL_BASE);
    }

    //Cadastra um cliente novo pela tela e abre o perfil dele.

    protected FluxoDeCadastro.Dados clienteNovoNoPerfil() {
        FluxoDeCadastro.Dados dados = cadastro().registrarClienteNovo();
        abrirPerfil();
        return dados;
    }

    protected void abrirPerfil() {
        abrir(PaginaPerfil.CAMINHO);
        esperarPor(PaginaPerfil.SECAO_ENDERECOS);
        esperarCampoPreenchido(PaginaPerfil.NOME);
    }

    /**
     * Entra pelo cartão de administrador e abre a aba de clientes.
     *
     * <p>Usado pelas suítes de RF0023 e RF0024. O cartão vem do seed
     * V999 — se a entrada falhar, o seed não foi aplicado.
     */
    protected void entrarComoAdministrador() {
        abrir(PaginaLogin.CAMINHO);
        clicar(PaginaLogin.CARTAO_ADMIN);
        esperarUrlConter("/admin");

        clicar(PaginaAdmin.ABA_CLIENTES);
        esperarPor(PaginaAdmin.BUSCA);
    }
}
