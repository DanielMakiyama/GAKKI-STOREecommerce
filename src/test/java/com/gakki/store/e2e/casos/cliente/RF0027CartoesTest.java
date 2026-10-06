package com.gakki.store.e2e.casos.cliente;

import com.gakki.store.e2e.ApiDeApoio;
import com.gakki.store.e2e.BaseE2ETest;
import com.gakki.store.e2e.paginas.PaginaPerfil;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebElement;

import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

//RF0027 — Cartões de crédito do cliente.

@DisplayName("RF0027 — Cartões de crédito do cliente")
class RF0027CartoesTest extends BaseE2ETest {

    private static final String ROXO = "Cartao Roxo";
    private static final String AZUL = "Cartao Azul";

    @BeforeEach
    void cadastrarClienteEAbrirPerfil() {
        clienteNovoNoPerfil();
        esperarPor(PaginaPerfil.SECAO_CARTOES);

        // Ponto de partida: nenhum cartão. O cadastro não exige um.
        assertThat(navegador.findElements(PaginaPerfil.CARTOES)).isEmpty();
    }

    @Test
    @DisplayName("Cadastra o primeiro cartão, que já nasce preferencial")
    void cadastraPrimeiroCartao() {
        adicionarCartao(ROXO, "4321", 12, YearMonth.now().getYear() + 3);

        esperarQuantidade(PaginaPerfil.CARTOES, 1);

        // O primeiro assume sozinho: sem isso o cliente teria cartões e
        // nenhum sugerido no checkout.
        assertThat(atributoDe(PaginaPerfil.cartao(ROXO), "data-preferencial")).isEqualTo("true");
    }

    @Test
    @DisplayName("RN0024 — recusa cartão com validade já vencida")
    void recusaCartaoVencido() {
        YearMonth agora = YearMonth.now();

        // O <select> de ano só oferece do ano corrente em diante, então
        // a única validade vencida que a tela consegue expressar é um mês
        // anterior dentro do ano corrente. Em janeiro isso é impossível,
        // e o teste é pulado em vez de falhar por um motivo que não é
        // defeito da aplicação.
        Assumptions.assumeTrue(agora.getMonthValue() > 1,
                "Em janeiro a tela não permite escolher uma validade vencida");

        adicionarCartao(ROXO, "4321", agora.getMonthValue() - 1, agora.getYear());

        assertThat(textoDe(PaginaPerfil.ERRO_CARTAO)).containsIgnoringCase("vencido");
        assertThat(navegador.findElements(PaginaPerfil.CARTOES)).isEmpty();
    }

    //RN0025 — a bandeira precisa estar registrada no sistema.

    @Test
    @DisplayName("RN0025 — a tela só oferece bandeiras cadastradas no sistema")
    void ofereceSomenteBandeirasCadastradas() {
        clicar(PaginaPerfil.BOTAO_NOVO_CARTAO);
        esperarPor(PaginaPerfil.CARTAO_BANDEIRA);

        List<String> naTela = navegador.findElements(PaginaPerfil.OPCOES_DE_BANDEIRA).stream()
                .map(WebElement::getText)
                .filter(texto -> !texto.startsWith("Selecione"))
                .toList();

        assertThat(naTela)
                .isNotEmpty()
                .containsExactlyInAnyOrderElementsOf(ApiDeApoio.nomesDasBandeiras());
    }

    @Test
    @DisplayName("Marca outro cartão como preferencial e o anterior deixa de ser")
    void trocaOCartaoPreferencial() {
        int ano = YearMonth.now().getYear() + 3;
        adicionarCartao(ROXO, "4321", 12, ano);
        esperarQuantidade(PaginaPerfil.CARTOES, 1);

        adicionarCartao(AZUL, "1234", 6, ano);
        esperarQuantidade(PaginaPerfil.CARTOES, 2);

        assertThat(atributoDe(PaginaPerfil.cartao(ROXO), "data-preferencial")).isEqualTo("true");
        assertThat(atributoDe(PaginaPerfil.cartao(AZUL), "data-preferencial")).isEqualTo("false");

        clicar(PaginaPerfil.acaoDoCartao(AZUL, "preferencial"));

        // O índice ux_cartao_preferencial não admite dois preferenciais
        // no mesmo cliente: se a troca não desmarcasse o anterior, o
        // banco recusaria e nenhum dos dois mudaria.
        esperarPreferencial(AZUL);
        assertThat(atributoDe(PaginaPerfil.cartao(ROXO), "data-preferencial")).isEqualTo("false");
    }

    @Test
    @DisplayName("Altera um cartão existente e a lista reflete a mudança")
    void alteraCartaoExistente() {
        adicionarCartao(ROXO, "4321", 12, YearMonth.now().getYear() + 3);
        esperarQuantidade(PaginaPerfil.CARTOES, 1);

        clicar(PaginaPerfil.acaoDoCartao(ROXO, "alterar"));
        substituir(PaginaPerfil.CARTAO_APELIDO, "Cartao da Empresa");
        clicar(PaginaPerfil.BOTAO_SALVAR_CARTAO);

        // Alteração, e não inclusão: continua sendo um cartão só.
        esperarPor(PaginaPerfil.cartao("Cartao da Empresa"));
        assertThat(navegador.findElements(PaginaPerfil.CARTOES)).hasSize(1);
        assertThat(navegador.findElements(PaginaPerfil.cartao(ROXO))).isEmpty();
    }

    @Test
    @DisplayName("Ao remover o preferencial, o próximo cartão assume")
    void promoveOProximoAoRemoverOPreferencial() {
        int ano = YearMonth.now().getYear() + 3;
        adicionarCartao(ROXO, "4321", 12, ano);
        esperarQuantidade(PaginaPerfil.CARTOES, 1);
        adicionarCartao(AZUL, "1234", 6, ano);
        esperarQuantidade(PaginaPerfil.CARTOES, 2);

        clicar(PaginaPerfil.acaoDoCartao(ROXO, "remover"));

        esperarQuantidade(PaginaPerfil.CARTOES, 1);
        assertThat(navegador.findElements(PaginaPerfil.cartao(ROXO))).isEmpty();

        // Sem a promoção, o cliente ficaria com cartões e nenhum
        // preferencial — e o checkout sem forma de pagamento sugerida.
        esperarPreferencial(AZUL);
    }

    /**
     * A assimetria com a RN0022, dita em um teste.
     *
     * <p>Remover o último endereço de entrega devolve 409; remover o
     * último cartão é permitido. Cartão é dado acessório de verdade — e,
     * como o endereço, sai do banco de fato, ao contrário do cadastro do
     * cliente, que é apenas inativado (RF0023).
     */
    @Test
    @DisplayName("Remover o último cartão é permitido — diferente do último endereço de entrega")
    void removeOUltimoCartao() {
        adicionarCartao(ROXO, "4321", 12, YearMonth.now().getYear() + 3);
        esperarQuantidade(PaginaPerfil.CARTOES, 1);

        clicar(PaginaPerfil.acaoDoCartao(ROXO, "remover"));

        esperarQuantidade(PaginaPerfil.CARTOES, 0);
        assertThat(navegador.findElements(PaginaPerfil.ERRO_CARTAO)).isEmpty();
    }

    /**
     * Abre o formulário, preenche e salva.
     *
     * <p>Não espera pelo resultado: os casos de sucesso esperam a lista
     * crescer e o da RN0024 espera a mensagem de erro. Quem chama sabe o
     * que vem depois.
     */
    private void adicionarCartao(String apelido, String digitos, int mes, int ano) {
        clicar(PaginaPerfil.BOTAO_NOVO_CARTAO);

        preencher(PaginaPerfil.CARTAO_APELIDO, apelido);
        preencher(PaginaPerfil.CARTAO_DIGITOS, digitos);

        // Pela posição, e não pelo valor: o value da opção é o id da
        // bandeira no banco, que muda a cada recriação do schema.
        selecionarPorIndice(PaginaPerfil.CARTAO_BANDEIRA, 1);

        selecionarPorValor(PaginaPerfil.CARTAO_MES, String.valueOf(mes));
        selecionarPorValor(PaginaPerfil.CARTAO_ANO, String.valueOf(ano));
        preencher(PaginaPerfil.CARTAO_TITULAR, "CLIENTE DE TESTE E2E");

        clicar(PaginaPerfil.BOTAO_SALVAR_CARTAO);
    }

    private void esperarPreferencial(String apelido) {
        espera.until(nav -> "true".equals(
                nav.findElement(PaginaPerfil.cartao(apelido)).getDomAttribute("data-preferencial")));
    }
}
