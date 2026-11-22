package com.gakki.store.service.ia;

// Uma mensagem da conversa enviada à IA, independente do provedor.
public record MensagemIa(Papel papel, String texto) {

    public enum Papel {
        SISTEMA,
        USUARIO,
        ASSISTENTE
    }

    public static MensagemIa sistema(String texto) {
        return new MensagemIa(Papel.SISTEMA, texto);
    }

    public static MensagemIa usuario(String texto) {
        return new MensagemIa(Papel.USUARIO, texto);
    }

    public static MensagemIa assistente(String texto) {
        return new MensagemIa(Papel.ASSISTENTE, texto);
    }
}
