package com.gakki.store.e2e.paginas;

import org.openqa.selenium.By;

/**
 * "Minhas trocas" (RF0041 a RF0045) e as notificações da RN0046.
 *
 * <p>Os seletores apontam para os três estados do DRS — EM_TROCA,
 * TROCA_AUTORIZADA e TROCADO. O protótipo trabalhava com seis, e os
 * nomes antigos não existem mais em lugar nenhum: nem na tela, nem no
 * enum, nem no CHECK da tabela.
 */
public final class PaginaTrocas {

    public static final String CAMINHO = "/trocas";

    public static final By TROCAS = By.cssSelector("[data-testid='troca']");
    public static final By SEM_TROCAS = By.cssSelector("[data-testid='sem-trocas']");

    /** RN0046 — notificação gerada na autorização da troca. */
    public static final By BLOCO_NOTIFICACOES = By.cssSelector("[data-testid='notificacoes']");
    public static final By NOTIFICACOES = By.cssSelector("[data-testid='notificacao']");

    public static By trocaDeNumero(String numero) {
        return By.cssSelector("[data-testid='troca'][data-numero='" + numero + "']");
    }

    public static By statusDaTroca(String numero) {
        return dentroDaTroca(numero, "troca-status");
    }

    public static By rotuloDaTroca(String numero) {
        return dentroDaTroca(numero, "troca-rotulo");
    }

    public static By valorDaTroca(String numero) {
        return dentroDaTroca(numero, "troca-valor");
    }

    /** RF0045 — só existe depois da confirmação de recebimento. */
    public static By cupomDaTroca(String numero) {
        return dentroDaTroca(numero, "troca-cupom");
    }

    public static By itensDaTroca(String numero) {
        return dentroDaTroca(numero, "item-troca");
    }

    private static By dentroDaTroca(String numero, String testid) {
        return By.cssSelector("[data-testid='troca'][data-numero='" + numero + "']"
                + " [data-testid='" + testid + "']");
    }

    private PaginaTrocas() {
    }
}
