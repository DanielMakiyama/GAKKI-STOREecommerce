package com.gakki.store.service.ia;

import java.util.List;

// Contrato com o serviço de IA. Trocar de provedor (Ollama, Vertex...) é
// escrever outra implementação; o resto do sistema não muda.
public interface ProvedorIa {

    String nome();

    // Devolve o texto da resposta, com no máximo limiteTokens tokens.
    // Lança IaIndisponivelException se falhar.
    String conversar(String modelo, List<MensagemIa> mensagens, int limiteTokens);

    // Modelos que o provedor tem prontos para uso.
    List<String> modelosInstalados();
}
