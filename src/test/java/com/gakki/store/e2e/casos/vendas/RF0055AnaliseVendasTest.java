package com.gakki.store.e2e.casos.vendas;

import com.gakki.store.e2e.ApiDeApoio;
import com.gakki.store.e2e.BancoDeApoio;
import com.gakki.store.e2e.BaseVendasE2ETest;
import com.gakki.store.e2e.paginas.PaginaAnaliseVendas;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

//RF0055 — análise do histórico de vendas por categoria, com o gráfico de linhas do RNF0043 e o limite de tempo do RNF0011.

@DisplayName("RF0055 — análise do histórico de vendas por categoria")
class RF0055AnaliseVendasTest extends BaseVendasE2ETest {

    private static final ZoneId FUSO_DA_LOJA = ZoneId.of("America/Sao_Paulo");
    private static final DateTimeFormatter MES_ANO = DateTimeFormatter.ofPattern("MM/yyyy");

    private static final String GUITARRAS = "Guitarras";
    private static final String TECLADOS = "Teclados";
    private static final String PEDAIS = "Pedais";
    private static final String INTERFACES = "Interfaces de Áudio";
    private static final String ACESSORIOS = "Acessórios";
    private static final List<String> TODAS = List.of(ACESSORIOS, GUITARRAS, INTERFACES, PEDAIS, TECLADOS);

    // Período coberto pela massa V1000.
    private static final LocalDate INICIO_DA_MASSA = LocalDate.of(2026, 4, 1);
    private static final LocalDate FIM_DA_MASSA = LocalDate.of(2026, 9, 30);

    // ------------------------------------------------------------
    // Cenário 1
    // ------------------------------------------------------------

    @Test
    @DisplayName("1. Uma categoria num período válido: cada mês é a soma das compras ENTREGUE")
    void umaCategoriaNumPeriodoValido() {
        entrarNaAnalise();
        consultar(INICIO_DA_MASSA, FIM_DA_MASSA, GUITARRAS);

        assertEquals(1, linhasNoGrafico(), "uma categoria escolhida, uma linha no gráfico");
        assertEquals(6, mesesNaTela(), "abril a setembro são seis meses");
        conferirComOBanco(INICIO_DA_MASSA, FIM_DA_MASSA, List.of(GUITARRAS));

        assertValor("1400.00", valorNaTela(GUITARRAS, YearMonth.of(2026, 4)), "Guitarras em 04/2026");
        assertValor("1505.00", valorNaTela(GUITARRAS, YearMonth.of(2026, 5)),
                "Guitarras em 05/2026 (guitarra mais cordas, que também são da categoria)");
        assertValor("0", valorNaTela(GUITARRAS, YearMonth.of(2026, 6)),
                "Guitarras em 06/2026 — mês sem venda entregue aparece como zero, e não some");
    }

    // ------------------------------------------------------------
    // Cenário 2
    // ------------------------------------------------------------

    @Test
    @DisplayName("2. Comparação entre duas ou mais categorias no mesmo gráfico")
    void comparacaoEntreCategorias() {
        entrarNaAnalise();
        consultar(INICIO_DA_MASSA, FIM_DA_MASSA, GUITARRAS, TECLADOS, PEDAIS);

        assertEquals(3, linhasNoGrafico(), "três categorias, três linhas");
        assertEquals(List.of(GUITARRAS, PEDAIS, TECLADOS), textos(PaginaAnaliseVendas.LEGENDA),
                "a legenda identifica cada linha, em ordem alfabética");
        assertEquals(3 * 6, navegador.findElements(PaginaAnaliseVendas.VALORES).size(),
                "um valor por categoria e por mês");
        assertTrue(navegador.findElements(PaginaAnaliseVendas.valoresDa(ACESSORIOS)).isEmpty(),
                "categoria não escolhida não entra na comparação");

        conferirComOBanco(INICIO_DA_MASSA, FIM_DA_MASSA, List.of(GUITARRAS, TECLADOS, PEDAIS));

        // Em julho Guitarras e Teclados empatam e Pedais fica abaixo: a
        // comparação só faz sentido se cada linha tiver o próprio valor.
        assertValor("2800.00", valorNaTela(GUITARRAS, YearMonth.of(2026, 7)), "Guitarras em 07/2026");
        assertValor("2800.00", valorNaTela(TECLADOS, YearMonth.of(2026, 7)), "Teclados em 07/2026");
        assertValor("700.00", valorNaTela(PEDAIS, YearMonth.of(2026, 7)), "Pedais em 07/2026");
    }

    // ------------------------------------------------------------
    // Cenário 3
    // ------------------------------------------------------------

    @Test
    @DisplayName("3. Gráfico de linhas em reais, agrupado por mês/ano")
    void graficoDeLinhasEmReaisPorMes() {
        entrarNaAnalise();
        consultar(INICIO_DA_MASSA, FIM_DA_MASSA, TODAS.toArray(String[]::new));

        // RNF0043 — linhas, e não barras ou pizza.
        assertEquals(TODAS.size(), linhasNoGrafico(), "uma linha por categoria");
        assertTrue(navegador.findElements(PaginaAnaliseVendas.BARRAS_DO_GRAFICO).isEmpty(),
                "o gráfico é de linhas, sem barras");

        // Eixo horizontal: um ponto por mês, no formato MM/AAAA.
        List<String> meses = mesesEntre(INICIO_DA_MASSA, FIM_DA_MASSA).stream()
                .map(mes -> mes.format(MES_ANO)).toList();
        List<String> eixoX = textos(PaginaAnaliseVendas.ROTULOS_DO_EIXO_X);
        assertEquals(meses.size(), mesesNaTela(), "um ponto por mês do período");
        assertEquals(meses.get(0), eixoX.get(0), "o eixo começa no mês de início");
        assertEquals(meses.get(meses.size() - 1), eixoX.get(eixoX.size() - 1), "o eixo termina no mês de fim");
        assertTrue(eixoX.stream().allMatch(rotulo -> rotulo.matches("\\d{2}/\\d{4}")),
                () -> "rótulos do eixo horizontal fora do formato MM/AAAA: " + eixoX);

        // Eixo vertical e valores em reais.
        List<String> eixoY = textos(PaginaAnaliseVendas.ROTULOS_DO_EIXO_Y);
        assertFalse(eixoY.isEmpty(), "o eixo vertical tem rótulos");
        assertTrue(eixoY.stream().allMatch(rotulo -> rotulo.startsWith("R$")),
                () -> "rótulos do eixo vertical sem R$: " + eixoY);
        for (WebElement celula : navegador.findElements(PaginaAnaliseVendas.VALORES)) {
            assertTrue(celula.getText().startsWith("R$"),
                    () -> "valor fora do formato em reais: " + celula.getText());
        }
    }

    // ------------------------------------------------------------
    // Cenário 4
    // ------------------------------------------------------------

    @Test
    @DisplayName("4. Alterar a data de início ou de fim gera um novo resultado")
    void alterarOPeriodoGeraNovoResultado() {
        entrarNaAnalise();

        consultar(INICIO_DA_MASSA, FIM_DA_MASSA, GUITARRAS, PEDAIS);
        assertEquals(6, mesesNaTela(), "abril a setembro");
        Map<String, BigDecimal> periodoCompleto = valoresDaTela();

        // Muda só o início, e no meio do mês: maio passa a contar a partir
        // do dia 15, e abril sai do gráfico.
        LocalDate meioDeMaio = LocalDate.of(2026, 5, 15);
        consultar(meioDeMaio, FIM_DA_MASSA, GUITARRAS, PEDAIS);
        assertEquals(5, mesesNaTela(), "maio a setembro");
        assertEquals("05/2026", primeiroMesNaTela(), "o gráfico começa no mês da nova data de início");
        conferirComOBanco(meioDeMaio, FIM_DA_MASSA, List.of(GUITARRAS, PEDAIS));

        // Muda só o fim.
        LocalDate fimDeJulho = LocalDate.of(2026, 7, 31);
        consultar(meioDeMaio, fimDeJulho, GUITARRAS, PEDAIS);
        assertEquals(3, mesesNaTela(), "maio a julho");
        conferirComOBanco(meioDeMaio, fimDeJulho, List.of(GUITARRAS, PEDAIS));

        assertNotEquals(periodoCompleto, valoresDaTela(), "períodos diferentes, resultados diferentes");
    }

    // ------------------------------------------------------------
    // Cenário 5
    // ------------------------------------------------------------

    @Test
    @DisplayName("5. Compra com status diferente de ENTREGUE não entra no cálculo")
    void compraForaDeEntregueNaoEntra() {
        // Pré-condição: a massa tem compras fora de ENTREGUE nestes meses.
        // Sem elas, o caso passaria sem provar nada.
        YearMonth abril = YearMonth.of(2026, 4);
        YearMonth junho = YearMonth.of(2026, 6);
        BigDecimal tecladosForaEmAbril = BancoDeApoio.totalForaDeEntregue(TECLADOS, inicioDe(abril), fimDe(abril));
        BigDecimal guitarrasForaEmJunho = BancoDeApoio.totalForaDeEntregue(GUITARRAS, inicioDe(junho), fimDe(junho));
        assertTrue(tecladosForaEmAbril.signum() > 0, "a V1000 deveria ter compra de teclado não entregue em abril");
        assertTrue(guitarrasForaEmJunho.signum() > 0, "a V1000 deveria ter compra de guitarra não entregue em junho");

        entrarNaAnalise();
        consultar(INICIO_DA_MASSA, FIM_DA_MASSA, TODAS.toArray(String[]::new));

        assertValor("0", valorNaTela(TECLADOS, abril),
                "Teclados em 04/2026 — só há compras REPROVADA e EM_TRANSPORTE nesse mês");
        assertValor("0", valorNaTela(GUITARRAS, junho),
                "Guitarras em 06/2026 — a única compra do mês não foi entregue");

        // Em todo mês e categoria, a tela mostra a soma das ENTREGUE e
        // nunca a soma com as demais.
        for (String categoria : TODAS) {
            for (YearMonth mes : mesesEntre(INICIO_DA_MASSA, FIM_DA_MASSA)) {
                BigDecimal entregue = BancoDeApoio.totalEntregue(categoria, inicioDe(mes), fimDe(mes));
                BigDecimal fora = BancoDeApoio.totalForaDeEntregue(categoria, inicioDe(mes), fimDe(mes));
                BigDecimal naTela = valorNaTela(categoria, mes);

                assertValor(entregue.toPlainString(), naTela, categoria + " em " + mes.format(MES_ANO));
                if (fora.signum() > 0) {
                    assertNotEquals(0, entregue.add(fora).compareTo(naTela),
                            categoria + " em " + mes.format(MES_ANO) + " incluiu compra não entregue");
                }
            }
        }
    }

    // ------------------------------------------------------------
    // Cenário 6
    // ------------------------------------------------------------

    @Test
    @DisplayName("6. Compra que passa a ENTREGUE aparece na consulta seguinte")
    void compraQuePassaAEntregueApareceNaConsultaSeguinte() {
        // O jogo de cordas é de duas categorias, e a venda tem de aparecer
        // nas duas.
        List<String> categoriasDaCompra = BancoDeApoio.categoriasDoInstrumento(CORDAS);
        assertTrue(categoriasDaCompra.size() >= 2,
                () -> "o caso usa um instrumento de mais de uma categoria; veio " + categoriasDaCompra);
        BigDecimal valorDoItem = ApiDeApoio.precoDoInstrumento(CORDAS);

        // O mês da compra é o mês de hoje, no fuso da loja.
        LocalDate hoje = LocalDate.now(FUSO_DA_LOJA);
        YearMonth mesAtual = YearMonth.from(hoje);

        entrarNaAnalise();
        consultar(hoje, hoje, categoriasDaCompra.toArray(String[]::new));
        Map<String, BigDecimal> antes = valoresDoMes(categoriasDaCompra, mesAtual);

        String numero = compraPelaApi(CORDAS, 1);
        String tokenAdmin = ApiDeApoio.tokenDoAdministrador();
        long pedidoId = ApiDeApoio.idDoPedido(token, numero);
        boolean estoqueDebitado = false;

        try {
            ApiDeApoio.confirmarPagamento(tokenAdmin, pedidoId);
            assertTexto("APROVADA", ApiDeApoio.statusDoPedido(token, numero), "status depois do pagamento");
            estoqueDebitado = true;

            // Aprovada e despachada, mas ainda não entregue: não conta.
            ApiDeApoio.despachar(tokenAdmin, pedidoId);
            consultar(hoje, hoje, categoriasDaCompra.toArray(String[]::new));
            assertEquals(antes, valoresDoMes(categoriasDaCompra, mesAtual),
                    "compra EM_TRANSPORTE não pode entrar no histórico");

            // Entregue: a consulta seguinte já mostra, sem cache.
            ApiDeApoio.confirmarEntrega(tokenAdmin, pedidoId);
            assertTexto("ENTREGUE", ApiDeApoio.statusDoPedido(token, numero), "status depois da entrega");

            consultar(hoje, hoje, categoriasDaCompra.toArray(String[]::new));
            Map<String, BigDecimal> depois = valoresDoMes(categoriasDaCompra, mesAtual);
            for (String categoria : categoriasDaCompra) {
                assertValor(antes.get(categoria).add(valorDoItem).toPlainString(), depois.get(categoria),
                        categoria + " em " + mesAtual.format(MES_ANO) + " depois da entrega do pedido " + numero);
            }
        } finally {
            // A aprovação debitou uma unidade (RN0028). Devolver deixa a
            // suíte repetível sem esgotar o estoque do seed.
            if (estoqueDebitado) {
                BancoDeApoio.reporEstoque(CORDAS, 1);
            }
        }
    }

    // ------------------------------------------------------------
    // Cenário 7
    // ------------------------------------------------------------

    @Test
    @DisplayName("7. Só o administrador acessa; os demais perfis são barrados")
    void somenteOAdministradorAcessa() {
        String consulta = caminhoDaConsulta(INICIO_DA_MASSA, FIM_DA_MASSA, GUITARRAS);

        // Pela API: a regra vale no backend, e não só na tela.
        assertEquals(200, ApiDeApoio.statusDeConsulta(consulta, ApiDeApoio.tokenDoAdministrador()),
                "administrador consulta");
        assertEquals(403, ApiDeApoio.statusDeConsulta(consulta, token),
                "cliente autenticado é barrado");
        assertEquals(401, ApiDeApoio.statusDeConsulta(consulta, null),
                "visitante sem login é barrado");

        // Pela tela: o cliente que tenta abrir o painel volta para a loja.
        entrarComoCliente();
        abrir("/admin");
        espera.until(ExpectedConditions.not(ExpectedConditions.urlContains("/admin")));
        assertTrue(navegador.findElements(PaginaAnaliseVendas.ABA).isEmpty(),
                "a aba de análise não aparece para o cliente");
    }

    // ------------------------------------------------------------
    // Cenário 8
    // ------------------------------------------------------------

    @Test
    @DisplayName("8. Tempo de resposta da consulta de até 1 segundo (RNF0011)")
    void tempoDeRespostaDeAteUmSegundo() {
        String tokenAdmin = ApiDeApoio.tokenDoAdministrador();

        // O caso mais pesado que a tela permite: o ano inteiro, todas as
        // categorias. Três medições seguidas, e todas têm de caber no
        // limite — o RNF fala de cada consulta, não da média.
        String consulta = caminhoDaConsulta(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31),
                TODAS.toArray(String[]::new));
        for (int i = 1; i <= 3; i++) {
            assertRapida(consulta, ApiDeApoio.duracaoDeConsulta(consulta, tokenAdmin));
        }
    }

    // ------------------------------------------------------------
    // Passos pela tela
    // ------------------------------------------------------------

    private void entrarNaAnalise() {
        entrarComoAdministradorEm(PaginaAnaliseVendas.ABA);
        // As caixas de categoria vêm de GET /categorias: só dá para
        // consultar depois que elas chegam.
        espera.until(nav -> !nav.findElements(PaginaAnaliseVendas.CATEGORIAS).isEmpty());
    }


    private void consultar(LocalDate inicio, LocalDate fim, String... categorias) {
        preencherData(PaginaAnaliseVendas.INICIO, inicio);
        preencherData(PaginaAnaliseVendas.FIM, fim);

        Set<String> pedidas = Set.of(categorias);
        List<String> naTela = new ArrayList<>();
        for (WebElement caixa : navegador.findElements(PaginaAnaliseVendas.CATEGORIAS)) {
            String nome = caixa.getDomAttribute("data-categoria");
            naTela.add(nome);
            if (caixa.isSelected() != pedidas.contains(nome)) {
                caixa.click();
            }
        }
        assertTrue(naTela.containsAll(pedidas),
                () -> "categorias pedidas " + pedidas + " não estão todas na tela: " + naTela);

        List<WebElement> tabelaAnterior = navegador.findElements(PaginaAnaliseVendas.TABELA);
        clicar(PaginaAnaliseVendas.BOTAO_CONSULTAR);
        if (!tabelaAnterior.isEmpty()) {
            espera.until(ExpectedConditions.stalenessOf(tabelaAnterior.get(0)));
        }
        esperarPor(PaginaAnaliseVendas.TABELA);
    }

    private void preencherData(By campo, LocalDate data) {
        WebElement elemento = esperarPor(campo);
        ((JavascriptExecutor) navegador).executeScript("""
                const campo = arguments[0];
                const definirValor = Object.getOwnPropertyDescriptor(HTMLInputElement.prototype, 'value').set;
                definirValor.call(campo, arguments[1]);
                campo.dispatchEvent(new Event('input', { bubbles: true }));
                """, elemento, data.toString());
        assertEquals(data.toString(), valorDe(campo), "data preenchida no campo");
    }

    // ------------------------------------------------------------
    // Leitura da tela
    // ------------------------------------------------------------

    private BigDecimal valorNaTela(String categoria, YearMonth mes) {
        return new BigDecimal(atributoDe(PaginaAnaliseVendas.valor(categoria, mes.format(MES_ANO)), "data-valor"));
    }

    private Map<String, BigDecimal> valoresDoMes(List<String> categorias, YearMonth mes) {
        Map<String, BigDecimal> valores = new LinkedHashMap<>();
        categorias.forEach(categoria -> valores.put(categoria, valorNaTela(categoria, mes)));
        return valores;
    }

    private Map<String, BigDecimal> valoresDaTela() {
        return navegador.findElements(PaginaAnaliseVendas.VALORES).stream().collect(Collectors.toMap(
                celula -> celula.getDomAttribute("data-categoria") + " " + celula.getDomAttribute("data-mes"),
                celula -> new BigDecimal(celula.getDomAttribute("data-valor")),
                (a, b) -> a,
                LinkedHashMap::new));
    }

    private int linhasNoGrafico() {
        return navegador.findElements(PaginaAnaliseVendas.LINHAS_DO_GRAFICO).size();
    }

    private int mesesNaTela() {
        return Integer.parseInt(atributoDe(PaginaAnaliseVendas.GRAFICO, "data-meses"));
    }

    private String primeiroMesNaTela() {
        return navegador.findElements(PaginaAnaliseVendas.VALORES).get(0).getDomAttribute("data-mes");
    }

    private List<String> textos(By seletor) {
        return navegador.findElements(seletor).stream().map(WebElement::getText).toList();
    }

    // ------------------------------------------------------------
    // Valores esperados, calculados no banco por outro caminho
    // ------------------------------------------------------------

    private void conferirComOBanco(LocalDate inicio, LocalDate fim, List<String> categorias) {
        for (String categoria : categorias) {
            for (YearMonth mes : mesesEntre(inicio, fim)) {
                LocalDate primeiroDia = mes.atDay(1).isBefore(inicio) ? inicio : mes.atDay(1);
                LocalDate ultimoDia = mes.atEndOfMonth().isAfter(fim) ? fim : mes.atEndOfMonth();

                BigDecimal esperado = BancoDeApoio.totalEntregue(categoria,
                        inicioDoDia(primeiroDia), inicioDoDia(ultimoDia.plusDays(1)));

                assertValor(esperado.toPlainString(), valorNaTela(categoria, mes),
                        categoria + " em " + mes.format(MES_ANO));
            }
        }
    }

    private static List<YearMonth> mesesEntre(LocalDate inicio, LocalDate fim) {
        List<YearMonth> meses = new ArrayList<>();
        for (YearMonth mes = YearMonth.from(inicio); !mes.isAfter(YearMonth.from(fim)); mes = mes.plusMonths(1)) {
            meses.add(mes);
        }
        return meses;
    }

    private static OffsetDateTime inicioDoDia(LocalDate dia) {
        return dia.atStartOfDay(FUSO_DA_LOJA).toOffsetDateTime();
    }

    private static OffsetDateTime inicioDe(YearMonth mes) {
        return inicioDoDia(mes.atDay(1));
    }

    private static OffsetDateTime fimDe(YearMonth mes) {
        return inicioDoDia(mes.plusMonths(1).atDay(1));
    }

    private static String caminhoDaConsulta(LocalDate inicio, LocalDate fim, String... categorias) {
        String parametrosDeCategoria = Arrays.stream(categorias)
                .map(categoria -> "&categorias=" + URLEncoder.encode(categoria, StandardCharsets.UTF_8))
                .collect(Collectors.joining());
        return "/analise/vendas?inicio=" + inicio + "&fim=" + fim + parametrosDeCategoria;
    }
}
