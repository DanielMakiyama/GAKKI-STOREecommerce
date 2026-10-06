package com.gakki.store.e2e;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
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
        HttpRequest requisicao = autenticada("/clientes", token).GET().build();
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
        JsonNode pagina = enviar(autenticada("/clientes?email=" + email, tokenAdmin).GET().build());

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
        enviarSemCorpo(autenticada("/clientes/" + clienteId + "/" + acao, tokenAdmin)
                .method("PATCH", HttpRequest.BodyPublishers.noBody())
                .build());
    }

    // ------------------------------------------------------------
    // Catálogo (RF0011) — rota pública, não leva token
    //
    // O teste conhece o instrumento pelo código do seed (INST-0005), não
    // pelo id: id é BIGSERIAL e muda a cada recriação do schema, então
    // fixá-lo no código do caso só adiaria uma quebra.
    // ------------------------------------------------------------

    public static long idDoInstrumento(String codigo) {
        return instrumento(codigo).get("id").asLong();
    }

    public static int estoqueDoInstrumento(String codigo) {
        return instrumento(codigo).get("quantidadeEstoque").asInt();
    }

    public static BigDecimal precoDoInstrumento(String codigo) {
        // asText e não asDouble: o valor é monetário e vira BigDecimal
        // sem passar por binário de ponto flutuante.
        return new BigDecimal(instrumento(codigo).get("valorVenda").asText());
    }

    private static JsonNode instrumento(String codigo) {
        JsonNode pagina = enviar(requisicao("/instrumentos?size=100").GET().build());
        for (JsonNode item : pagina.get("content")) {
            if (codigo.equals(item.get("codigo").asText())) {
                return item;
            }
        }
        throw new IllegalStateException(
                "Nenhum instrumento com o código " + codigo + ". O seed V999 foi aplicado?");
    }

    // ------------------------------------------------------------
    // Carrinho (RF0031, RF0032)
    // ------------------------------------------------------------

    /**
     * Deixa o carrinho vazio.
     *
     * <p>Cada caso precisa disso no início: o carrinho é 1:1 com o
     * cliente e sobrevive entre execuções, então sobra de um caso
     * anterior entraria na conta do seguinte. Não existe
     * {@code DELETE /carrinho}, por isso a remoção é item a item.
     */
    public static void limparCarrinho(String token) {
        JsonNode carrinho = enviar(autenticada("/carrinho", token).GET().build());
        for (JsonNode item : carrinho.get("itens")) {
            long itemId = item.get("itemId").asLong();
            enviarSemCorpo(autenticada("/carrinho/itens/" + itemId, token).DELETE().build());
        }
    }

    /** Atalho para montar carrinho sem gastar passos de tela. */
    public static void adicionarAoCarrinho(String token, long instrumentoId, int quantidade) {
        String corpo = """
                {"instrumentoId":%d,"quantidade":%d}""".formatted(instrumentoId, quantidade);
        enviar(autenticada("/carrinho/itens", token)
                .POST(HttpRequest.BodyPublishers.ofString(corpo))
                .header("Content-Type", "application/json")
                .build());
    }

    public static BigDecimal totalDoCarrinho(String token) {
        JsonNode carrinho = enviar(autenticada("/carrinho", token).GET().build());
        return new BigDecimal(carrinho.get("valorTotal").asText());
    }

    // ------------------------------------------------------------
    // Endereços, cartões e cupons do cliente autenticado
    // ------------------------------------------------------------

    public static long idDoEnderecoPrincipal(String token) {
        JsonNode enderecos = enviar(autenticada("/clientes/me/enderecos", token).GET().build());
        for (JsonNode endereco : enderecos) {
            if (endereco.get("principal").asBoolean()) {
                return endereco.get("id").asLong();
            }
        }
        return enderecos.get(0).get("id").asLong();
    }

    public static int quantidadeDeEnderecos(String token) {
        JsonNode enderecos = enviar(autenticada("/clientes/me/enderecos", token).GET().build());
        int total = 0;
        for (JsonNode ignorado : enderecos) {
            total++;
        }
        return total;
    }

    public static int quantidadeDeCartoes(String token) {
        JsonNode cartoes = enviar(autenticada("/clientes/me/cartoes", token).GET().build());
        int total = 0;
        for (JsonNode ignorado : cartoes) {
            total++;
        }
        return total;
    }

    /**
     * Códigos dos cupons do cliente.
     *
     * <p>Comparar a lista antes e depois da compra é como o caso da
     * RN0036 identifica o cupom de troco: o código vem da sequence da
     * V14 e incrementa a cada emissão, então afirmar
     * {@code "TROCA-1000"} funcionaria só na primeira execução.
     */
    public static List<String> codigosDosCupons(String token) {
        JsonNode cupons = enviar(autenticada("/cupons", token).GET().build());
        List<String> codigos = new ArrayList<>();
        cupons.forEach(cupom -> codigos.add(cupom.get("codigo").asText()));
        return codigos;
    }

    // ------------------------------------------------------------
    // RNF0011 — tempo de resposta das consultas
    // ------------------------------------------------------------

    /**
     * Quanto tempo um GET levou, em milissegundos.
     *
     * <p>Mede a consulta no backend, não a renderização da tela: o
     * RNF0011 fala de tempo de resposta, e cravar um limite incluindo
     * React, rede local e pintura do navegador mediria a máquina, não o
     * sistema.
     *
     * <p>{@code token} nulo faz a chamada sem autenticação, para as
     * rotas públicas.
     */
    public static long duracaoDeConsulta(String caminho, String token) {
        HttpRequest.Builder construtor = token == null
                ? requisicao(caminho)
                : autenticada(caminho, token);

        long inicio = System.nanoTime();
        enviar(construtor.GET().build());
        return (System.nanoTime() - inicio) / 1_000_000L;
    }

    // ------------------------------------------------------------
    // Plumbing
    // ------------------------------------------------------------

    private static HttpRequest.Builder requisicao(String caminho) {
        return HttpRequest.newBuilder(URI.create(URL_API + caminho))
                .timeout(Duration.ofSeconds(15));
    }

    private static HttpRequest.Builder autenticada(String caminho, String token) {
        return requisicao(caminho).header("Authorization", "Bearer " + token);
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
