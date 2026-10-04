package com.gakki.store.e2e.acoes;

import com.gakki.store.e2e.GeradorDeDados;
import com.gakki.store.e2e.paginas.PaginaRegistrar;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

//Preenche e submete o formulário de cadastro (RF0021).

public final class FluxoDeCadastro {
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

    //Apelido do endereço criado junto com o cadastro (RN0023).
    public static final String APELIDO_DO_ENDERECO_INICIAL = "Casa";

    private final WebDriver navegador;
    private final WebDriverWait espera;
    private final String urlBase;

    public FluxoDeCadastro(WebDriver navegador, WebDriverWait espera, String urlBase) {
        this.navegador = navegador;
        this.espera = espera;
        this.urlBase = urlBase;
    }

    public Dados registrarClienteNovo() {
        Dados dados = Dados.validos();
        preencher(dados);
        submeter();
        espera.until(ExpectedConditions.urlContains("/catalogo"));
        return dados;
    }

    //Preenche o formulário inteiro sem submeter.
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
