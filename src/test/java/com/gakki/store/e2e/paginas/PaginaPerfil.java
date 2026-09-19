package com.gakki.store.e2e.paginas;

import org.openqa.selenium.By;

/**
 * Perfil do cliente — seção de endereços (RF0026).
 *
 * <p>Os itens da lista são localizados pelo <b>apelido</b>, e não pelo
 * id: o id só existe depois que o backend salva, e o teste precisaria
 * lê-lo de volta da tela antes de poder clicar em qualquer coisa. O
 * apelido é escolhido pelo teste, é o que o usuário enxerga e é único
 * dentro de cada cenário.
 */
public final class PaginaPerfil {

    public static final String CAMINHO = "/perfil";

    public static final By CODIGO = By.cssSelector("[data-testid='codigo-cliente']");

    // Dados cadastrais (RF0022)
    public static final By ERRO_DADOS = By.cssSelector("[data-testid='erro-dados']");
    public static final By SUCESSO_DADOS = By.cssSelector("[data-testid='sucesso-dados']");
    public static final By BOTAO_SALVAR_DADOS = By.cssSelector("[data-testid='btn-salvar-dados']");

    public static final By NOME = By.id("perf-nome");
    public static final By EMAIL = By.id("perf-email");
    public static final By CPF = By.id("perf-cpf");
    public static final By GENERO = By.id("perf-genero");
    public static final By NASCIMENTO = By.id("perf-nascimento");
    public static final By TELEFONE_TIPO = By.id("perf-tel-tipo");
    public static final By TELEFONE_DDD = By.id("perf-tel-ddd");
    public static final By TELEFONE_NUMERO = By.id("perf-tel-numero");

    // Senha (RF0028)
    public static final By ERRO_SENHA = By.cssSelector("[data-testid='erro-senha']");
    public static final By SUCESSO_SENHA = By.cssSelector("[data-testid='sucesso-senha']");
    public static final By BOTAO_SALVAR_SENHA = By.cssSelector("[data-testid='btn-salvar-senha']");

    public static final By SENHA_ATUAL = By.id("perf-senha-atual");
    public static final By SENHA_NOVA = By.id("perf-senha-nova");
    public static final By SENHA_CONFIRMACAO = By.id("perf-senha-confirmacao");

    // Endereços (RF0026)
    public static final By SECAO_ENDERECOS = By.cssSelector("[data-testid='secao-enderecos']");
    public static final By ERRO_ENDERECO = By.cssSelector("[data-testid='erro-endereco']");
    public static final By BOTAO_NOVO_ENDERECO = By.cssSelector("[data-testid='btn-novo-endereco']");
    public static final By BOTAO_SALVAR_ENDERECO = By.cssSelector("[data-testid='btn-salvar-endereco']");

    /** Todos os endereços da lista — serve para contar. */
    public static final By ENDERECOS = By.cssSelector("[data-endereco]");

    // Formulário de endereço (o mesmo para cadastrar e para alterar:
    // a tela nunca abre os dois ao mesmo tempo).
    public static final By END_APELIDO = By.id("end-apelido");
    public static final By END_TIPO_RESIDENCIA = By.id("end-tipo-residencia");
    public static final By END_TIPO_LOGRADOURO = By.id("end-tipo-logradouro");
    public static final By END_LOGRADOURO = By.id("end-logradouro");
    public static final By END_NUMERO = By.id("end-numero");
    public static final By END_COMPLEMENTO = By.id("end-complemento");
    public static final By END_BAIRRO = By.id("end-bairro");
    public static final By END_CIDADE = By.id("end-cidade");
    public static final By END_UF = By.id("end-uf");
    public static final By END_CEP = By.id("end-cep");
    public static final By END_PAIS = By.id("end-pais");
    public static final By END_OBSERVACOES = By.id("end-observacoes");
    public static final By END_ENTREGA = By.id("end-entrega");
    public static final By END_COBRANCA = By.id("end-cobranca");

    private PaginaPerfil() {
    }

    /** O item da lista com este apelido. */
    public static By endereco(String apelido) {
        return By.cssSelector("[data-apelido='" + apelido + "']");
    }

    /**
     * Botão de ação dentro do item: {@code principal}, {@code alterar} ou
     * {@code remover}.
     */
    public static By acao(String apelido, String acao) {
        return By.cssSelector("[data-apelido='" + apelido + "'] [data-acao='" + acao + "']");
    }
}
