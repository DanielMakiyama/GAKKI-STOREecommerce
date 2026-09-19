package com.gakki.store.e2e.casos;

import com.gakki.store.e2e.ApiDeApoio;
import com.gakki.store.e2e.BaseE2ETest;
import com.gakki.store.e2e.paginas.PaginaAdmin;
import com.gakki.store.e2e.paginas.PaginaLogin;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RF0024 — Consulta de clientes por filtro.
 *
 * <p>O requisito é literal: "todos os campos utilizados para
 * identificação do cliente podem ser utilizados como filtro, tanto de
 * forma combinada como de forma isolada". Os testes cobrem as duas
 * metades — cada campo sozinho, e a combinação.
 *
 * <p>Os cenários usam os clientes do seed V999, cujos valores estão
 * fixados no contrato:
 *
 * <ul>
 *   <li>CLI-0001 Cliente Demonstração — ativo</li>
 *   <li>CLI-0002 Maria Souza — ativo</li>
 *   <li>CLI-0003 João Pereira — ativo</li>
 *   <li>CLI-0004 Beatriz Lima — <b>inativo</b></li>
 * </ul>
 *
 * <p>Nenhum cenário depende da contagem total da tabela, e isso é
 * deliberado: as outras suítes cadastram clientes a cada execução, e uma
 * asserção sobre o total quebraria na segunda rodada. Os filtros aqui
 * apontam para dados que só o seed tem.
 */
@DisplayName("RF0024 — Consulta de clientes por filtro")
class RF0024ConsultarClienteTest extends BaseE2ETest {

    private static final String CODIGO_MARIA = "CLI-0002";
    private static final String CODIGO_JOAO = "CLI-0003";
    private static final String CODIGO_BEATRIZ = "CLI-0004";

    @Test
    @DisplayName("Filtro isolado por nome traz só quem corresponde")
    void filtraPorNome() {
        entrarComoAdministrador();

        filtrar(PaginaAdmin.BUSCA, "Maria");

        esperarTotal(1);
        assertThat(navegador.findElements(PaginaAdmin.linhaComCodigo(CODIGO_MARIA))).hasSize(1);
        assertThat(navegador.findElements(PaginaAdmin.linhaComCodigo(CODIGO_JOAO))).isEmpty();
    }

    @Test
    @DisplayName("Filtro isolado por código do cadastro (RNF0035)")
    void filtraPorCodigo() {
        entrarComoAdministrador();

        // Código é comparação exata, ao contrário de nome e e-mail.
        filtrar(PaginaAdmin.FILTRO_CODIGO, CODIGO_JOAO);

        esperarTotal(1);
        assertThat(navegador.findElements(PaginaAdmin.linhaComCodigo(CODIGO_JOAO))).hasSize(1);
    }

    @Test
    @DisplayName("Filtro isolado por CPF")
    void filtraPorCpf() {
        entrarComoAdministrador();

        // CPF da Maria Souza, do seed. Também é comparação exata — e o
        // backend compara só os dígitos, então a máscara não atrapalha.
        filtrar(PaginaAdmin.FILTRO_CPF, "295.289.264-44");

        esperarTotal(1);
        assertThat(navegador.findElements(PaginaAdmin.linhaComCodigo(CODIGO_MARIA))).hasSize(1);
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
        esperarPor(PaginaAdmin.linhaComCodigo(CODIGO_BEATRIZ));
        assertThat(navegador.findElements(PaginaAdmin.linhaComCodigo(CODIGO_MARIA))).isEmpty();
    }

    /**
     * O caso que prova a combinação, e não apenas a coexistência dos
     * campos.
     *
     * <p>"Lima" sozinho encontra a Beatriz. "Somente ativos" sozinho
     * encontra vários. Juntos precisam encontrar <b>nenhum</b>, porque a
     * Beatriz está inativa. Um backend que ignorasse o segundo filtro, ou
     * que trocasse o {@code AND} por {@code OR}, passaria nos quatro
     * testes anteriores e falharia neste.
     */
    @Test
    @DisplayName("Filtros combinados: nome + status se aplicam em conjunto, não em alternativa")
    void combinaNomeEStatus() {
        entrarComoAdministrador();

        filtrar(PaginaAdmin.BUSCA, "Lima");
        esperarTotal(1);
        assertThat(navegador.findElements(PaginaAdmin.linhaComCodigo(CODIGO_BEATRIZ))).hasSize(1);

        // Mesmo nome, agora exigindo que esteja ativa.
        selecionarPorValor(PaginaAdmin.FILTRO_STATUS, "ativos");
        clicar(PaginaAdmin.BOTAO_FILTRAR);
        esperarTotal(0);

        // E, exigindo inativa, ela volta.
        selecionarPorValor(PaginaAdmin.FILTRO_STATUS, "inativos");
        clicar(PaginaAdmin.BOTAO_FILTRAR);
        esperarTotal(1);
        assertThat(navegador.findElements(PaginaAdmin.linhaComCodigo(CODIGO_BEATRIZ))).hasSize(1);
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
    private void filtrar(org.openqa.selenium.By campo, String valor) {
        clicar(PaginaAdmin.BOTAO_LIMPAR);
        preencher(campo, valor);
        clicar(PaginaAdmin.BOTAO_FILTRAR);
    }

    /**
     * Espera a contagem exibida bater com a esperada.
     *
     * <p>Esperar, e não conferir na hora: entre o clique em "Filtrar" e a
     * resposta do servidor a tabela ainda mostra o resultado anterior.
     */
    private void esperarTotal(int esperado) {
        espera.until(nav ->
                String.valueOf(esperado).equals(nav.findElement(PaginaAdmin.TOTAL).getText()));
    }
}
