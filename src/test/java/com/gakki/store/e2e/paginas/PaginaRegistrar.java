package com.gakki.store.e2e.paginas;

import org.openqa.selenium.By;

/**
 * Tela de cadastro (RF0021).
 *
 * <p>Só seletores — a mecânica de preencher e submeter fica no
 * {@link com.gakki.store.e2e.acoes.FluxoDeCadastro}. Assim, quando o
 * front mudar uma classe de CSS, muda um arquivo, e nenhum teste.
 */
public final class PaginaRegistrar {

    public static final String CAMINHO = "/registrar";

    // Dados pessoais (RN0026)
    public static final By NOME = By.id("cad-nome");
    public static final By EMAIL = By.id("cad-email");
    public static final By CPF = By.id("cad-cpf");
    public static final By GENERO = By.id("cad-genero");
    public static final By NASCIMENTO = By.id("cad-nascimento");
    public static final By TELEFONE_TIPO = By.id("cad-tel-tipo");
    public static final By TELEFONE_DDD = By.id("cad-tel-ddd");
    public static final By TELEFONE_NUMERO = By.id("cad-tel-numero");
    public static final By SENHA = By.id("cad-senha");
    public static final By CONFIRMACAO = By.id("cad-senha2");

    // Endereço (RN0023)
    public static final By APELIDO = By.id("cad-apelido");
    public static final By TIPO_RESIDENCIA = By.id("cad-tipo-residencia");
    public static final By TIPO_LOGRADOURO = By.id("cad-tipo-logradouro");
    public static final By LOGRADOURO = By.id("cad-logradouro");
    public static final By NUMERO = By.id("cad-numero");
    public static final By BAIRRO = By.id("cad-bairro");
    public static final By CIDADE = By.id("cad-cidade");
    public static final By UF = By.id("cad-uf");
    public static final By CEP = By.id("cad-cep");
    public static final By PAIS = By.id("cad-pais");

    public static final By BOTAO_CRIAR = By.cssSelector("[data-testid='btn-criar-conta']");
    public static final By ERRO = By.cssSelector("[data-testid='erro-cadastro']");

    private PaginaRegistrar() {
    }
}
