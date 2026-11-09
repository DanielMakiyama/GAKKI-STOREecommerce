package com.gakki.store.e2e.paginas;

import org.openqa.selenium.By;

/**
 * Aba "Análise de vendas" do painel administrativo (RF0055).
 *
 * <p>Os seletores usam os {@code data-testid} e {@code data-*} que a tela
 * expõe para os testes, e não textos de interface: o rótulo de um botão
 * pode mudar sem que nenhuma regra mude.
 *
 * <p>Os valores são lidos da tabela que acompanha o gráfico
 * ({@code data-valor}), e não do desenho: um SVG guarda coordenadas em
 * pixel, e converter pixel de volta em reais seria testar a escala do
 * eixo, não a soma das vendas. Do gráfico em si o teste confere o que o
 * RNF0043 pede — que é de linhas, uma por categoria, com os meses no eixo
 * horizontal e reais no vertical.
 */
public final class PaginaAnaliseVendas {

    public static final By ABA = By.cssSelector("[data-aba='analise']");

    // Filtros
    public static final By INICIO = By.cssSelector("[data-testid='analise-inicio']");
    public static final By FIM = By.cssSelector("[data-testid='analise-fim']");
    public static final By CATEGORIAS = By.cssSelector("[data-testid='analise-categoria']");
    public static final By BOTAO_CONSULTAR = By.cssSelector("[data-testid='btn-consultar-analise']");
    public static final By ERRO = By.cssSelector("[data-testid='erro-analise']");

    // Gráfico (Recharts). As classes recharts-* são as que a biblioteca
    // gera (conferidas na 3.10); o data-testid do contêiner é nosso. Se
    // uma atualização do Recharts renomear alguma, é aqui que se ajusta.
    public static final By GRAFICO = By.cssSelector("[data-testid='grafico-historico-vendas']");
    public static final By LINHAS_DO_GRAFICO =
            By.cssSelector("[data-testid='grafico-historico-vendas'] path.recharts-line-curve");
    public static final By BARRAS_DO_GRAFICO =
            By.cssSelector("[data-testid='grafico-historico-vendas'] .recharts-bar-rectangle");
    public static final By LEGENDA =
            By.cssSelector("[data-testid='grafico-historico-vendas'] .recharts-legend-item-text");
    public static final By ROTULOS_DO_EIXO_X =
            By.cssSelector("[data-testid='grafico-historico-vendas'] .recharts-xAxis-tick-labels .recharts-cartesian-axis-tick-value");
    public static final By ROTULOS_DO_EIXO_Y =
            By.cssSelector("[data-testid='grafico-historico-vendas'] .recharts-yAxis-tick-labels .recharts-cartesian-axis-tick-value");

    // Tabela de valores
    public static final By TABELA = By.cssSelector("[data-testid='tabela-historico-vendas']");
    public static final By VALORES = By.cssSelector("[data-testid='valor-historico']");

    private PaginaAnaliseVendas() {
    }

    public static By categoria(String nome) {
        return By.cssSelector("[data-testid='analise-categoria'][data-categoria='" + nome + "']");
    }

    /** Célula de uma categoria num mês; {@code mesAno} no formato da tela, MM/AAAA. */
    public static By valor(String categoria, String mesAno) {
        return By.cssSelector("[data-testid='valor-historico'][data-categoria='" + categoria
                + "'][data-mes='" + mesAno + "']");
    }

    /** Todas as células de uma categoria, uma por mês do período. */
    public static By valoresDa(String categoria) {
        return By.cssSelector("[data-testid='valor-historico'][data-categoria='" + categoria + "']");
    }
}
