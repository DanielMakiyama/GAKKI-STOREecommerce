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
    // Carrinho — itens retirados (RNF0042)
    // ------------------------------------------------------------

    /**
     * Descarta tudo que a expiração do prazo retirou do carrinho.
     *
     * <p>Precisa rodar no preparo de cada caso: item retirado deixado por
     * um caso anterior desabilita a compra no seguinte (RNF0042), e o
     * caso falharia por sujeira, não por defeito. A rota é idempotente,
     * então chamar com nada retirado não é erro.
     */
    public static void descartarItensRetirados(String token) {
        enviarSemCorpo(autenticada("/carrinho/retirados", token).DELETE().build());
    }

    public static int itensRetiradosNoCarrinho(String token) {
        return enviar(autenticada("/carrinho", token).GET().build()).get("itensRetirados").size();
    }

    public static boolean carrinhoPodeComprar(String token) {
        return enviar(autenticada("/carrinho", token).GET().build()).get("podeComprar").asBoolean();
    }

    // ------------------------------------------------------------
    // Pedido — leitura e transições de administrador
    //
    // As transições entram aqui, e não nos casos, porque na maior parte
    // deles levar o pedido até ENTREGUE é PREPARO: o que está sob teste é
    // a troca, que só existe depois da entrega (RN0043). O caso que
    // demonstra o despacho em si faz os mesmos passos pela tela.
    // ------------------------------------------------------------

    /** Cria a compra pela API, com o carrinho já montado. Devolve o número. */
    public static String finalizarCompra(String token, long enderecoEntregaId, long cartaoId) {
        String corpo = """
                {"enderecoEntregaId":%d,"codigosCupom":[],
                 "pagamentosCartao":[{"cartaoCreditoId":%d,"valor":%s}]}"""
                .formatted(enderecoEntregaId, cartaoId, totalComFrete(token, enderecoEntregaId));

        return enviar(autenticada("/pedidos", token)
                .POST(HttpRequest.BodyPublishers.ofString(corpo))
                .header("Content-Type", "application/json")
                .build())
                .get("numero").asText();
    }

    private static BigDecimal totalComFrete(String token, long enderecoEntregaId) {
        return new BigDecimal(enviar(autenticada(
                "/carrinho/frete?enderecoEntregaId=" + enderecoEntregaId, token).GET().build())
                .get("valorTotalComFrete").asText());
    }

    /**
     * Primeiro cartão do cliente.
     *
     * <p>Qual deles não importa para quem só precisa de uma compra criada:
     * o caso que verifica a composição do pagamento (RN0034) escolhe os
     * cartões pela tela, um por um.
     */
    public static long idDoPrimeiroCartao(String token) {
        JsonNode cartoes = enviar(autenticada("/clientes/me/cartoes", token).GET().build());
        if (cartoes.isEmpty()) {
            throw new IllegalStateException(
                    "O cliente de demonstração não tem cartão. O seed V999 foi aplicado?");
        }
        return cartoes.get(0).get("id").asLong();
    }

    private static JsonNode pedido(String token, String numero) {
        JsonNode pagina = enviar(autenticada("/pedidos?size=100&sort=id,desc", token).GET().build());
        for (JsonNode p : pagina.get("content")) {
            if (numero.equals(p.get("numero").asText())) {
                return p;
            }
        }
        throw new IllegalStateException("Nenhum pedido com o número " + numero);
    }

    public static long idDoPedido(String token, String numero) {
        return pedido(token, numero).get("id").asLong();
    }

    public static String statusDoPedido(String token, String numero) {
        return pedido(token, numero).get("status").asText();
    }

    /** Ids dos itens do pedido, na ordem em que a API devolve. */
    public static List<Long> idsDosItens(String token, String numero) {
        List<Long> ids = new ArrayList<>();
        for (JsonNode item : pedido(token, numero).get("itens")) {
            ids.add(item.get("itemPedidoId").asLong());
        }
        return ids;
    }

    public static void confirmarPagamento(String tokenAdmin, long pedidoId) {
        acaoDePedido(tokenAdmin, pedidoId, "confirmar-pagamento");
    }

    public static void despachar(String tokenAdmin, long pedidoId) {
        acaoDePedido(tokenAdmin, pedidoId, "despachar");
    }

    public static void confirmarEntrega(String tokenAdmin, long pedidoId) {
        acaoDePedido(tokenAdmin, pedidoId, "confirmar-entrega");
    }

    private static void acaoDePedido(String tokenAdmin, long pedidoId, String acao) {
        enviar(autenticada("/pedidos/" + pedidoId + "/" + acao, tokenAdmin)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build());
    }

    // ------------------------------------------------------------
    // Troca (RF0041 a RF0045) e notificação (RN0046)
    // ------------------------------------------------------------

    public static String solicitarTroca(String token, long pedidoId, long itemPedidoId, int quantidade) {
        String corpo = """
                {"pedidoId":%d,"itens":[{"itemPedidoId":%d,"quantidade":%d}]}"""
                .formatted(pedidoId, itemPedidoId, quantidade);

        return enviar(autenticada("/trocas", token)
                .POST(HttpRequest.BodyPublishers.ofString(corpo))
                .header("Content-Type", "application/json")
                .build())
                .get("numero").asText();
    }

    public static long idDaTroca(String token, String numero) {
        for (JsonNode troca : enviar(autenticada("/trocas?size=100&sort=id,desc", token).GET().build())
                .get("content")) {
            if (numero.equals(troca.get("numero").asText())) {
                return troca.get("id").asLong();
            }
        }
        throw new IllegalStateException("Nenhuma troca com o número " + numero);
    }

    public static void autorizarTroca(String tokenAdmin, long trocaId) {
        enviar(autenticada("/trocas/" + trocaId + "/autorizar", tokenAdmin)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build());
    }

    public static int quantidadeDeNotificacoes(String token) {
        return enviar(autenticada("/notificacoes?size=100", token).GET().build())
                .get("content").size();
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
