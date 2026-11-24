package com.gakki.store.e2e.paginas;

import org.openqa.selenium.By;

// Chatbot de recomendação (RNF0044), o widget flutuante do Chatbot.jsx.
public final class PaginaChat {

    public static final By BOTAO_ABRIR = By.cssSelector("[data-testid='chat-abrir']");
    public static final By JANELA = By.cssSelector("[data-testid='chat-janela']");
    public static final By ENTRADA = By.cssSelector("[data-testid='chat-input']");
    public static final By BOTAO_ENVIAR = By.cssSelector("[data-testid='chat-enviar']");
    public static final By NOVA_CONVERSA = By.cssSelector("[data-testid='chat-nova-conversa']");
    public static final By PENSANDO = By.cssSelector("[data-testid='chat-digitando']");
    public static final By RESPOSTAS = By.cssSelector("[data-testid='chat-resposta']");
    public static final By AVISO = By.cssSelector("[data-testid='chat-aviso']");

    // Botões e cartões dentro de uma resposta específica, pelo id gravado no banco.
    public static By resposta(long mensagemId) {
        return By.cssSelector("[data-testid='chat-resposta'][data-mensagem-id='" + mensagemId + "']");
    }

    public static By botaoUtil(long mensagemId) {
        return By.cssSelector("[data-mensagem-id='" + mensagemId + "'] [data-testid='chat-util']");
    }

    public static By botaoNaoUtil(long mensagemId) {
        return By.cssSelector("[data-mensagem-id='" + mensagemId + "'] [data-testid='chat-nao-util']");
    }

    public static By produtoCitado(long mensagemId) {
        return By.cssSelector("[data-mensagem-id='" + mensagemId + "'] [data-testid='chat-produto']");
    }

    private PaginaChat() {
    }
}
