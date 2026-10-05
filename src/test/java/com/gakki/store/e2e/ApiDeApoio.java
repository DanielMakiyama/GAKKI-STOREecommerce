package com.gakki.store.e2e;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

//Atalho pela API, usado só para preparar e limpar estado.

public final class ApiDeApoio {

    private static final String URL_API = System.getProperty("e2e.api", "http://localhost:8080/api/v1");
    private static final HttpClient CLIENTE = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private static final ObjectMapper JSON = new ObjectMapper();

    private ApiDeApoio() {
    }

    public static String token(String email, String senha) {
        String corpo = """
                {"email":"%s","senha":"%s"}""".formatted(email, senha);
        JsonNode resposta = enviar(requisicao("/auth/login")
                .POST(HttpRequest.BodyPublishers.ofString(corpo))
                .header("Content-Type", "application/json")
                .build());
        return resposta.get("token").asText();
    }

    public static String tokenDoAdministrador() {
        return token("admin@gakkistore.com", "Admin@2026");
    }

    public static String tokenDoClienteDeDemonstracao() {
        return token("cliente@gakkistore.com", "Cliente@2026");
    }

    public static int statusDaListagemDeClientes(String token) {
        HttpRequest requisicao = requisicao("/clientes")
                .GET()
                .header("Authorization", "Bearer " + token)
                .build();
        try {
            return CLIENTE.send(requisicao, HttpResponse.BodyHandlers.ofString()).statusCode();
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao chamar " + requisicao.uri(), e);
        }
    }

    //Nomes das bandeiras cadastradas no sistema (RN0025).

    public static List<String> nomesDasBandeiras() {
        JsonNode resposta = enviar(requisicao("/bandeiras").GET().build());

        List<String> nomes = new ArrayList<>();
        resposta.forEach(bandeira -> nomes.add(bandeira.get("nome").asText()));
        return nomes;
    }

    //Id do cliente a partir do e-mail, via consulta administrativa (RF0024)
    public static long idDoCliente(String tokenAdmin, String email) {
        JsonNode pagina = enviar(requisicao("/clientes?email=" + email)
                .GET()
                .header("Authorization", "Bearer " + tokenAdmin)
                .build());

        JsonNode conteudo = pagina.get("content");
        if (conteudo == null || conteudo.isEmpty()) {
            throw new IllegalStateException(
                    "Nenhum cliente com o e-mail " + email + ". O seed V999 foi aplicado?");
        }
        return conteudo.get(0).get("id").asLong();
    }

    public static void inativar(String tokenAdmin, long clienteId) {
        alternarStatus(tokenAdmin, clienteId, "inativar");
    }

    public static void ativar(String tokenAdmin, long clienteId) {
        alternarStatus(tokenAdmin, clienteId, "ativar");
    }

    private static void alternarStatus(String tokenAdmin, long clienteId, String acao) {
        enviarSemCorpo(requisicao("/clientes/" + clienteId + "/" + acao)
                .method("PATCH", HttpRequest.BodyPublishers.noBody())
                .header("Authorization", "Bearer " + tokenAdmin)
                .build());
    }

    private static HttpRequest.Builder requisicao(String caminho) {
        return HttpRequest.newBuilder(URI.create(URL_API + caminho))
                .timeout(Duration.ofSeconds(15));
    }

    private static JsonNode enviar(HttpRequest requisicao) {
        try {
            HttpResponse<String> resposta = CLIENTE.send(requisicao, HttpResponse.BodyHandlers.ofString());
            conferirSucesso(requisicao, resposta);
            return JSON.readTree(resposta.body());
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao chamar " + requisicao.uri(), e);
        }
    }

    private static void enviarSemCorpo(HttpRequest requisicao) {
        try {
            conferirSucesso(requisicao, CLIENTE.send(requisicao, HttpResponse.BodyHandlers.ofString()));
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao chamar " + requisicao.uri(), e);
        }
    }

    private static void conferirSucesso(HttpRequest requisicao, HttpResponse<String> resposta) {
        if (resposta.statusCode() >= 300) {
            throw new IllegalStateException(
                    "HTTP " + resposta.statusCode() + " em " + requisicao.uri() + ": " + resposta.body());
        }
    }
}
