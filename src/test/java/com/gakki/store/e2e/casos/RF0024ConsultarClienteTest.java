package com.gakki.store.e2e.casos;

import com.gakki.store.e2e.ApiDeApoio;
import com.gakki.store.e2e.BaseE2ETest;
import com.gakki.store.e2e.paginas.PaginaAdmin;
import com.gakki.store.e2e.paginas.PaginaLogin;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

import java.util.Arrays;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RF0024 — Consulta de clientes por filtro.
 *
 * <p>O requisito é literal: "todos os campos utilizados para
 * identificação do cliente podem ser utilizados como filtro, tanto de
 * forma combinada como de forma isolada". Os testes cobrem os cinco
 * campos isolados e a combinação.
 *
 * <p>Os cenários usam os clientes do seed V999, fixados no contrato:
 *
 * <ul>
 *   <li>CLI-0001 Cliente Demonstração — ativo</li>
 *   <li>CLI-0002 Maria Souza — ativo</li>
 *   <li>CLI-0003 João Pereira — ativo</li>
 *   <li>CLI-0004 Beatriz Lima — <b>inativo</b></li>
 * </ul>
 *
 * <p><b>Nenhuma asserção conta linhas.</b> A primeira versão desta suíte
 * esperava "total = 1" e quebrou: as outras suítes cadastram um cliente
 * por teste, então a tabela cresce a cada execução e qualquer número
 * fixo tem prazo de validade. O que se afirma aqui é <i>quem</i> aparece
 * e <i>quem não</i> — que é o que o requisito realmente exige, e vale
 * para um banco com quatro clientes ou quatrocentos.
 */
@DisplayName("RF0024 — Consulta de clientes por filtro")
class RF0024ConsultarClienteTest extends BaseE2ETest {

    private static final String DEMONSTRACAO = "CLI-0001";
    private static final String MARIA = "CLI-0002";
    private static final String JOAO = "CLI-0003";
    private static final String BEATRIZ = "CLI-0004";

    @Test
    @DisplayName("Filtro isolado por nome")
    void filtraPorNome() {
        entrarComoAdministrador();

        // "Souza" e não "Maria": é o sobrenome dela no seed e não
        // aparece em nenhum cliente criado pelas outras suítes.
        filtrar(PaginaAdmin.BUSCA, "Souza");

        esperarListagem(MARIA, DEMONSTRACAO, JOAO, BEATRIZ);
    }

    @Test
    @DisplayName("Filtro isolado por e-mail")
    void filtraPorEmail() {
        entrarComoAdministrador();

        // Busca parcial: o filtro de e-mail é por conteúdo, como o de nome.
        filtrar(PaginaAdmin.FILTRO_EMAIL, "joao.pereira");

        esperarListagem(JOAO, DEMONSTRACAO, MARIA, BEATRIZ);
    }

    @Test
    @DisplayName("Filtro isolado por código do cadastro (RNF0035)")
    void filtraPorCodigo() {
        entrarComoAdministrador();

        // Código é comparação exata, ao contrário de nome e e-mail.
        filtrar(PaginaAdmin.FILTRO_CODIGO, JOAO);

        esperarListagem(JOAO, DEMONSTRACAO, MARIA, BEATRIZ);
    }

    @Test
    @DisplayName("Filtro isolado por CPF, com máscara")
    void filtraPorCpf() {
        entrarComoAdministrador();

        // CPF da Maria Souza. Comparação exata, e o backend olha só os
        // dígitos — a máscara digitada na tela não atrapalha.
        filtrar(PaginaAdmin.FILTRO_CPF, "295.289.264-44");

        esperarListagem(MARIA, DEMONSTRACAO, JOAO, BEATRIZ);
    }

    @Test
    @DisplayName("Filtro isolado por status traz os inativos — que continuam cadastrados")
    void filtraPorStatusInativo() {
        entrarComoAdministrador();

        selecionarPorValor(PaginaAdmin.FILTRO_STATUS, "inativos");
        clicar(PaginaAdmin.BOTAO_FILTRAR);

        // Beatriz nasce inativa no seed. Ela aparecer aqui é a outra
        // face da RF0023: inativar não exclui — o cadastro continua
        // consultável, só não autentica.
        esperarListagem(BEATRIZ, DEMONSTRACAO, MARIA, JOAO);
    }

    /**
     * O caso que prova a combinação, e não a mera coexistência dos campos.
     *
     * <p>"Lima" sozinho encontra a Beatriz. "Somente ativos" sozinho
     * encontra vários. Juntos precisam encontrar <b>nenhum</b>, porque ela
     * está inativa. Um backend que ignorasse o segundo filtro, ou que
     * trocasse o {@code AND} por {@code OR}, passaria em todos os testes
     * anteriores e falharia neste.
     */
    @Test
    @DisplayName("Filtros combinados: nome + status se aplicam em conjunto, não em alternativa")
    void combinaNomeEStatus() {
        entrarComoAdministrador();

        filtrar(PaginaAdmin.BUSCA, "Lima");
        esperarListagem(BEATRIZ, DEMONSTRACAO, MARIA, JOAO);

        // Mesmo nome, agora exigindo que esteja ativa.
        selecionarPorValor(PaginaAdmin.FILTRO_STATUS, "ativos");
        clicar(PaginaAdmin.BOTAO_FILTRAR);
        esperarListagemVazia();

        // E, exigindo inativa, ela volta.
        selecionarPorValor(PaginaAdmin.FILTRO_STATUS, "inativos");
        clicar(PaginaAdmin.BOTAO_FILTRAR);
        esperarListagem(BEATRIZ, DEMONSTRACAO, MARIA, JOAO);
    }

    /**
     * A consulta administrativa é privativa do administrador.
     *
     * <p>A prova tem duas camadas, e a segunda é a que importa: o front
     * desvia o cliente para fora de {@code /admin} antes de qualquer
     * requisição sair, mas isso protege a navegação, não o dado. Quem
     * protege o dado é o {@code @PreAuthorize} do controller — e um
     * cliente com token válido chamando a API direto tem que levar 403.
     */
    @Test
    @DisplayName("Cliente não acessa a consulta administrativa (403)")
    void clienteNaoConsultaOutrosClientes() {
        abrir(PaginaLogin.CAMINHO);
        clicar(PaginaLogin.CARTAO_CLIENTE);
        esperarUrlConter("/catalogo");

        abrir(PaginaAdmin.CAMINHO);

        // O front tira o cliente da rota administrativa.
        espera.until(nav -> !nav.getCurrentUrl().contains("/admin"));
        assertThat(navegador.findElements(PaginaAdmin.BUSCA)).isEmpty();

        // E o backend recusa por conta própria, sem depender da tela.
        int status = ApiDeApoio.statusDaListagemDeClientes(ApiDeApoio.tokenDoClienteDeDemonstracao());
        assertThat(status).isEqualTo(403);
    }

    /** Limpa os filtros, preenche um só e aplica. */
    private void filtrar(By campo, String valor) {
        clicar(PaginaAdmin.BOTAO_LIMPAR);
        preencher(campo, valor);
        clicar(PaginaAdmin.BOTAO_FILTRAR);
    }

    /**
     * Espera a listagem conter um cadastro e não conter os outros.
     *
     * <p>Esperar pelas <b>duas</b> condições juntas é o que elimina a
     * corrida: entre o clique em "Filtrar" e a resposta do servidor, a
     * tabela ainda mostra o resultado anterior — onde o esperado já está
     * presente, junto com todos os demais. Conferir só a presença
     * aprovaria a tela não filtrada.
     */
    private void esperarListagem(String presente, String... ausentes) {
        try {
            espera.until(nav ->
                    temLinha(presente)
                            && Arrays.stream(ausentes).noneMatch(this::temLinha));
        } catch (TimeoutException e) {
            throw new AssertionError(diagnostico(
                    "Esperava " + presente + " presente e " + Arrays.toString(ausentes) + " ausentes"), e);
        }
    }

    private void esperarListagemVazia() {
        try {
            espera.until(nav -> nav.findElements(PaginaAdmin.LINHAS).isEmpty());
        } catch (TimeoutException e) {
            throw new AssertionError(diagnostico("Esperava listagem vazia"), e);
        }
    }

    private boolean temLinha(String codigo) {
        return !navegador.findElements(PaginaAdmin.linhaComCodigo(codigo)).isEmpty();
    }

    /**
     * Descreve o que estava na tela quando a espera estourou.
     *
     * <p>Um {@code TimeoutException} cru só diz que a condição não foi
     * satisfeita. Saber quais cadastros estavam listados, e se havia
     * mensagem de erro, é a diferença entre diagnosticar na hora e
     * precisar de outra execução para descobrir.
     */
    private String diagnostico(String esperado) {
        String listados = navegador.findElements(PaginaAdmin.LINHAS).stream()
                .map(linha -> linha.getDomAttribute("data-codigo"))
                .collect(Collectors.joining(", "));

        String erro = navegador.findElements(PaginaAdmin.ERRO).stream()
                .findFirst()
                .map(WebElement::getText)
                .orElse("(nenhuma)");

        return esperado
                + ". Na tela: [" + listados + "]"
                + ". Mensagem de erro exibida: " + erro;
    }
}
