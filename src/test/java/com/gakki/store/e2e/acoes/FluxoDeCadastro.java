package com.gakki.store.e2e.acoes;

import com.gakki.store.e2e.GeradorDeDados;
import com.gakki.store.e2e.paginas.PaginaRegistrar;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Preenche e submete o formulário de cadastro (RF0021).
 *
 * <p>Existe porque duas suítes precisam do mesmo formulário por motivos
 * diferentes: a RF0021 o exercita — é o que ela testa — e a RF0026 só
 * precisa de um cliente novo para ter endereços seus com que mexer.
 * Duplicar vinte {@code sendKeys} nos dois lugares significaria que um
 * campo novo no front quebra duas suítes e se conserta em dois arquivos.
 *
 * <p>É uma <b>ação</b>, não uma página: o {@link PaginaRegistrar} diz
 * onde os campos estão, e esta classe diz o que fazer com eles.
 */
public final class FluxoDeCadastro {

    /**
     * Os quatro campos que os testes variam.
     *
     * <p>O resto do formulário é preenchido sempre igual — nenhum caso de
     * teste depende do bairro. Deixar só estes quatro visíveis mantém a
     * intenção de cada teste legível na chamada.
     */
    public record Dados(String email, String cpf, String senha, String confirmacao) {

        public static Dados validos() {
            String senha = GeradorDeDados.senhaForte();
            return new Dados(GeradorDeDados.emailUnico(), GeradorDeDados.cpfValido(), senha, senha);
        }

        public Dados comEmail(String outro) {
            return new Dados(outro, cpf, senha, confirmacao);
        }

        public Dados comCpf(String outro) {
            return new Dados(email, outro, senha, confirmacao);
        }

        public Dados comSenhas(String nova, String novaConfirmacao) {
            return new Dados(email, cpf, nova, novaConfirmacao);
        }
    }

    /** Apelido do endereço criado junto com o cadastro (RN0023). */
    public static final String APELIDO_DO_ENDERECO_INICIAL = "Casa";

    private final WebDriver navegador;
    private final WebDriverWait espera;
    private final String urlBase;

    public FluxoDeCadastro(WebDriver navegador, WebDriverWait espera, String urlBase) {
        this.navegador = navegador;
        this.espera = espera;
        this.urlBase = urlBase;
    }

    /**
     * Cadastra um cliente novo e devolve os dados usados.
     *
     * <p>Ao fim, o navegador já está autenticado no catálogo: o cadastro
     * autentica em seguida, então não há um segundo passo de login — o
     * que é bom, porque a tela de entrada só oferece as duas contas de
     * demonstração do seed, e não um formulário de e-mail e senha.
     *
     * <p>O cliente nasce com um endereço só, de entrega <b>e</b> cobrança
     * e marcado como principal. É esse estado inicial que a suíte de
     * endereços usa para provar a RN0022.
     */
    public Dados registrarClienteNovo() {
        Dados dados = Dados.validos();
        preencher(dados);
        submeter();
        espera.until(ExpectedConditions.urlContains("/catalogo"));
        return dados;
    }

    /** Preenche o formulário inteiro, sem submeter. */
    public FluxoDeCadastro preencher(Dados dados) {
        navegador.get(urlBase + PaginaRegistrar.CAMINHO);

        preencher(PaginaRegistrar.NOME, "Cliente de Teste E2E");
        preencher(PaginaRegistrar.EMAIL, dados.email());
        preencher(PaginaRegistrar.CPF, dados.cpf());
        selecionar(PaginaRegistrar.GENERO, "MASCULINO");
        preencher(PaginaRegistrar.NASCIMENTO, "14/05/2000");
        selecionar(PaginaRegistrar.TELEFONE_TIPO, "CELULAR");
        preencher(PaginaRegistrar.TELEFONE_DDD, "11");
        preencher(PaginaRegistrar.TELEFONE_NUMERO, "988887777");
        preencher(PaginaRegistrar.SENHA, dados.senha());
        preencher(PaginaRegistrar.CONFIRMACAO, dados.confirmacao());

        preencher(PaginaRegistrar.APELIDO, APELIDO_DO_ENDERECO_INICIAL);
        selecionar(PaginaRegistrar.TIPO_RESIDENCIA, "APARTAMENTO");
        selecionar(PaginaRegistrar.TIPO_LOGRADOURO, "RUA");
        preencher(PaginaRegistrar.LOGRADOURO, "das Guitarras");
        preencher(PaginaRegistrar.NUMERO, "123");
        preencher(PaginaRegistrar.BAIRRO, "Centro");
        preencher(PaginaRegistrar.CIDADE, "Sao Paulo");
        preencher(PaginaRegistrar.UF, "SP");
        preencher(PaginaRegistrar.CEP, "01000000");
        preencher(PaginaRegistrar.PAIS, "Brasil");

        return this;
    }

    public void submeter() {
        espera.until(ExpectedConditions.elementToBeClickable(PaginaRegistrar.BOTAO_CRIAR)).click();
    }

    private void preencher(By seletor, String valor) {
        WebElement campo = espera.until(ExpectedConditions.visibilityOfElementLocated(seletor));
        campo.clear();
        campo.sendKeys(valor);
    }

    private void selecionar(By seletor, String valor) {
        new Select(espera.until(ExpectedConditions.visibilityOfElementLocated(seletor)))
                .selectByValue(valor);
    }
}
