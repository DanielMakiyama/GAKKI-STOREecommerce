package com.gakki.store.service.ia;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gakki.store.config.IaProperties;
import com.gakki.store.exception.IaIndisponivelException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

// RNF0044 — modelo aberto servido localmente pelo Ollama (API REST na porta 11434).
@Component
@RequiredArgsConstructor
public class OllamaProvedorIa implements ProvedorIa {

    private final RestClient iaRestClient;
    private final IaProperties propriedades;

    @Override
    public String nome() {
        return "OLLAMA";
    }

    @Override
    public String conversar(String modelo, List<MensagemIa> mensagens, int limiteTokens) {
        PedidoChat pedido = new PedidoChat(
                modelo,
                mensagens.stream().map(OllamaProvedorIa::paraOllama).toList(),
                false,
                propriedades.keepAlive(),
                new Opcoes(propriedades.numGpu(), propriedades.numCtx(), limiteTokens, propriedades.temperatura()));

        RespostaChat resposta;
        try {
            resposta = iaRestClient.post()
                    .uri("/api/chat")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(pedido)
                    .retrieve()
                    .body(RespostaChat.class);
        } catch (RestClientException e) {
            throw new IaIndisponivelException("Falha ao consultar o Ollama em " + propriedades.url(), e);
        }

        if (resposta == null || resposta.message() == null
                || resposta.message().content() == null || resposta.message().content().isBlank()) {
            throw new IaIndisponivelException("O Ollama devolveu uma resposta vazia.");
        }
        return resposta.message().content().trim();
    }

    @Override
    public List<String> modelosInstalados() {
        try {
            RespostaTags tags = iaRestClient.get().uri("/api/tags").retrieve().body(RespostaTags.class);
            return tags == null || tags.models() == null
                    ? List.of()
                    : tags.models().stream().map(ModeloInstalado::name).sorted().toList();
        } catch (RestClientException e) {
            throw new IaIndisponivelException("Ollama fora do ar em " + propriedades.url(), e);
        }
    }

    private static Mensagem paraOllama(MensagemIa mensagem) {
        String papel = switch (mensagem.papel()) {
            case SISTEMA -> "system";
            case USUARIO -> "user";
            case ASSISTENTE -> "assistant";
        };
        return new Mensagem(papel, mensagem.texto());
    }

    // Formato JSON da API do Ollama (/api/chat e /api/tags).

    record PedidoChat(String model,
                      List<Mensagem> messages,
                      boolean stream,
                      @JsonProperty("keep_alive") String keepAlive,
                      Opcoes options) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    record Opcoes(@JsonProperty("num_gpu") Integer numGpu,
                  @JsonProperty("num_ctx") Integer numCtx,
                  @JsonProperty("num_predict") Integer numPredict,
                  Double temperature) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Mensagem(String role, String content) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record RespostaChat(Mensagem message) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record RespostaTags(List<ModeloInstalado> models) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record ModeloInstalado(String name) {
    }
}
