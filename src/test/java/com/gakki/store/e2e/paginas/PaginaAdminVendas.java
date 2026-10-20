package com.gakki.store.e2e.paginas;

import org.openqa.selenium.By;

/**
 * As abas de Pedidos e Trocas do painel administrativo
 * (RF0039, RF0040, RF0042 a RF0045).
 *
 * <p>Arquivo próprio em vez de crescer o {@code PaginaAdmin}: aquele
 * atende o módulo Cliente (aba de clientes, busca por filtros) e esta
 * fatia não precisa mexer nele. Mesmo critério que fez o
 * {@code BaseVendasE2ETest} estender o {@code BaseE2ETest} em vez de
 * alterá-lo.
 *
 * <p>As abas são identificadas por {@code data-aba}, e não pelo rótulo:
 * rótulo é texto de interface e muda; a chave da aba, não.
 */
public final class PaginaAdminVendas {

    public static final String CAMINHO = "/admin";

    public static final By ABA_PEDIDOS = By.cssSelector("[data-aba='pedidos']");
    public static final By ABA_TROCAS = By.cssSelector("[data-aba='trocas']");

    // ------------------------------------------------------------
    // Pedidos (RF0039, RF0040)
    // ------------------------------------------------------------

    public static final By PEDIDOS = By.cssSelector("[data-testid='pedido-admin']");
    public static final By FILTRO_STATUS_PEDIDO = By.cssSelector("[data-testid='filtro-status-pedido']");

    public static By pedidoDeNumero(String numero) {
        return By.cssSelector("[data-testid='pedido-admin'][data-numero='" + numero + "']");
    }

    /**
     * O botão da próxima ação do pedido.
     *
     * <p>Um só por cartão: o rótulo muda conforme o status
     * ("Confirmar pagamento", "Despachar", "Confirmar entrega"), e é
     * justamente essa sequência que o caso de RF0039 percorre.
     */
    public static By acaoDoPedido(String numero) {
        return By.cssSelector("[data-testid='pedido-admin'][data-numero='" + numero + "']"
                + " [data-testid='btn-acao-pedido']");
    }

    /*
     * O status do pedido é lido do atributo `data-status` do cartão, e
     * não de um elemento próprio: a tag visível tem text-transform
     * uppercase no CSS, então o texto renderizado não é o valor do enum.
     * O atributo carrega o valor cru, que é o que a asserção quer.
     */

    // ------------------------------------------------------------
    // Trocas (RF0042 a RF0045)
    // ------------------------------------------------------------

    public static final By TROCAS = By.cssSelector("[data-testid='troca-admin']");
    public static final By SEM_TROCAS = By.cssSelector("[data-testid='sem-trocas-admin']");
    public static final By MENSAGEM = By.cssSelector("[data-testid='mensagem-troca']");
    public static final By FILTRO_STATUS_TROCA = By.cssSelector("[data-testid='filtro-status-troca']");

    public static By trocaDeNumero(String numero) {
        return By.cssSelector("[data-testid='troca-admin'][data-numero='" + numero + "']");
    }

    public static By statusDaTroca(String numero) {
        return dentroDaTroca(numero, "troca-admin-status");
    }

    /** RF0042 — autorizar, o que dispara a notificação da RN0046. */
    public static By botaoAutorizar(String numero) {
        return dentroDaTroca(numero, "btn-autorizar-troca");
    }

    /** RF0044 — recebimento com retorno ao estoque. */
    public static By botaoReceberComEstoque(String numero) {
        return dentroDaTroca(numero, "btn-receber-troca-com-estoque");
    }

    /** RF0044 — recebimento sem retorno ao estoque. */
    public static By botaoReceberSemEstoque(String numero) {
        return dentroDaTroca(numero, "btn-receber-troca-sem-estoque");
    }

    /** O que foi decidido e qual cupom saiu (RF0044, RF0045). */
    public static By desfechoDaTroca(String numero) {
        return dentroDaTroca(numero, "troca-admin-desfecho");
    }

    private static By dentroDaTroca(String numero, String testid) {
        return By.cssSelector("[data-testid='troca-admin'][data-numero='" + numero + "']"
                + " [data-testid='" + testid + "']");
    }

    private PaginaAdminVendas() {
    }
}
