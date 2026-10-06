package com.gakki.store.e2e.paginas;

import org.openqa.selenium.By;

// Checkout — endereço (RF0035, RN0023), pagamento (RF0036, RF0037,
// RN0024, RN0025, RN0033 a RN0036) e finalização (RF0038).
//
// As linhas de pagamento são repetidas: SELECT_DO_CARTAO e VALOR_DA_LINHA
// casam com várias linhas ao mesmo tempo, e o teste usa findElements com
// índice. Dar um seletor único por linha exigiria um id vindo do estado,
// e o estado é um array sem chave estável.
public final class PaginaCheckout {

    public static final String CAMINHO = "/checkout";

    public static final By ERRO = By.cssSelector("[data-testid='erro-checkout']");

    // ---------- Endereço de entrega ----------

    public static final By OPCOES_DE_ENDERECO = By.cssSelector("[data-testid='opcao-endereco']");

    public static final By BOTAO_NOVO_ENDERECO = By.cssSelector("[data-testid='btn-novo-endereco']");
    public static final By ENDERECO_APELIDO = By.cssSelector("[data-testid='endereco-apelido']");
    public static final By ENDERECO_LOGRADOURO = By.cssSelector("[data-testid='endereco-logradouro']");
    public static final By ENDERECO_NUMERO = By.cssSelector("[data-testid='endereco-numero']");
    public static final By ENDERECO_COMPLEMENTO = By.cssSelector("[data-testid='endereco-complemento']");
    public static final By ENDERECO_BAIRRO = By.cssSelector("[data-testid='endereco-bairro']");
    public static final By ENDERECO_CIDADE = By.cssSelector("[data-testid='endereco-cidade']");
    public static final By ENDERECO_ESTADO = By.cssSelector("[data-testid='endereco-estado']");
    public static final By ENDERECO_CEP = By.cssSelector("[data-testid='endereco-cep']");
    public static final By BOTAO_SALVAR_ENDERECO = By.cssSelector("[data-testid='btn-salvar-endereco']");

    // O radio carrega o id do endereço no `value`, igual ao catálogo.
    public static By opcaoDeEndereco(long enderecoId) {
        return By.cssSelector("[data-testid='opcao-endereco'][value='" + enderecoId + "']");
    }

    // ---------- Pagamento ----------

    public static final By LINHA_DE_PAGAMENTO = By.cssSelector("[data-testid='linha-pagamento']");
    public static final By SELECT_DO_CARTAO = By.cssSelector("[data-testid='pagamento-cartao']");
    public static final By VALOR_DA_LINHA = By.cssSelector("[data-testid='pagamento-valor']");
    public static final By BOTAO_RESTANTE = By.cssSelector("[data-testid='btn-restante']");
    public static final By BOTAO_REMOVER_PAGAMENTO = By.cssSelector("[data-testid='btn-remover-pagamento']");

    public static final By BOTAO_USAR_CARTAO = By.cssSelector("[data-testid='btn-usar-cartao']");
    public static final By BOTAO_CADASTRAR_CARTAO = By.cssSelector("[data-testid='btn-cadastrar-cartao']");

    public static final By CARTAO_APELIDO = By.cssSelector("[data-testid='cartao-apelido']");
    public static final By CARTAO_DIGITOS = By.cssSelector("[data-testid='cartao-digitos']");
    public static final By CARTAO_TITULAR = By.cssSelector("[data-testid='cartao-titular']");

    // Três <select>, não campos de texto: a bandeira sai da lista do
    // sistema (RN0025) e a validade vai em mês e ano separados, como o
    // CartaoRequest espera.
    public static final By CARTAO_BANDEIRA = By.cssSelector("[data-testid='cartao-bandeira']");
    public static final By CARTAO_MES = By.cssSelector("[data-testid='cartao-mes']");
    public static final By CARTAO_ANO = By.cssSelector("[data-testid='cartao-ano']");

    public static final By BOTAO_SALVAR_CARTAO = By.cssSelector("[data-testid='btn-salvar-cartao']");

    // ---------- Cupons ----------

    public static final By CAMPO_CUPOM = By.cssSelector("[data-testid='input-cupom']");
    public static final By BOTAO_ADICIONAR_CUPOM = By.cssSelector("[data-testid='btn-adicionar-cupom']");
    public static final By TAGS_DE_CUPOM = By.cssSelector("[data-testid='tag-cupom']");

    public static By tagDoCupom(String codigo) {
        return By.cssSelector("[data-testid='tag-cupom'][data-codigo='" + codigo + "']");
    }

    // ---------- Resumo ----------

    public static final By RESUMO_SUBTOTAL = By.cssSelector("[data-testid='resumo-subtotal']");
    public static final By RESUMO_FRETE = By.cssSelector("[data-testid='resumo-frete']");
    public static final By RESUMO_TOTAL = By.cssSelector("[data-testid='resumo-total']");
    public static final By RESUMO_ALOCADO = By.cssSelector("[data-testid='resumo-alocado']");
    public static final By RESUMO_CUPONS = By.cssSelector("[data-testid='resumo-cupons']");
    public static final By FALTA_ALOCAR = By.cssSelector("[data-testid='falta-alocar']");

    public static final By BOTAO_CONFIRMAR = By.cssSelector("[data-testid='btn-confirmar-pedido']");

    // ---------- Confirmação (RF0038) ----------

    public static final By CONFIRMACAO = By.cssSelector("[data-testid='confirmacao-pedido']");
    public static final By PEDIDO_NUMERO = By.cssSelector("[data-testid='pedido-numero']");
    public static final By PEDIDO_STATUS = By.cssSelector("[data-testid='pedido-status']");
    public static final By PEDIDO_TOTAL = By.cssSelector("[data-testid='pedido-total']");

    private PaginaCheckout() {
    }
}
