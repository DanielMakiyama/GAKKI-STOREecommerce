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

    protected void selecionarPorValor(By seletor, String valor) {
        new Select(esperarPor(seletor)).selectByValue(valor);
    }

    /**
     * Seleciona pela posição na lista.
     *
     * <p>Para campos cujo {@code value} é um id do banco — a bandeira do
     * cartão, por exemplo. O id muda a cada recriação do schema, então
     * fixá-lo no teste é amarrá-lo a um banco específico.
     */
    protected void selecionarPorIndice(By seletor, int indice) {
        new Select(esperarPor(seletor)).selectByIndex(indice);
    }

    /**
     * Deixa a caixa de seleção no estado pedido.
     *
     * <p>Clicar direto <b>alterna</b>: um {@code click()} numa caixa já
     * marcada a desmarca. Como o formulário de endereço nasce com
     * "entrega" marcada e "cobrança" não, um teste que só clicasse
     * produziria estados diferentes conforme o campo — e falharia por
     * motivo nenhum.
     */
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

    /**
     * Substitui o conteúdo de um campo que já vem preenchido.
     *
     * <p>Seleciona tudo e digita por cima, em vez de {@code clear()}: num
     * campo controlado pelo React, o {@code clear()} zera o DOM sem
     * passar pelo {@code onChange}, e o próximo render pode devolver o
     * valor antigo. O Ctrl+A seguido de digitação é uma edição normal,
     * indistinguível da que uma pessoa faria.
     */
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

    /**
     * Espera um campo deixar de estar vazio.
     *
     * <p>O perfil renderiza com o formulário em branco e o preenche
     * quando a resposta do {@code GET /clientes/me} chega. Conferir o
     * valor sem esperar por isso lê a tela antes do banco.
     */
    protected void esperarCampoPreenchido(By seletor) {
        espera.until(nav -> {
            String valor = nav.findElement(seletor).getDomProperty("value");
            return valor != null && !valor.isBlank();
        });
    }

    /** Fluxo de cadastro ligado ao navegador desta execução. */
    protected FluxoDeCadastro cadastro() {
        return new FluxoDeCadastro(navegador, espera, URL_BASE);
    }

    /**
     * Cadastra um cliente novo pela tela e abre o perfil dele.
     *
     * <p>Devolve os dados usados, porque o e-mail e a senha fazem falta
     * nos testes que precisam autenticar de novo depois.
     *
     * <p>Um cliente novo por teste é mais lento do que reaproveitar o do
     * seed, e é o que mantém as suítes independentes: nenhuma enxerga o
     * que a outra deixou para trás, e o cliente de demonstração continua
     * intacto para a apresentação.
     */
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
