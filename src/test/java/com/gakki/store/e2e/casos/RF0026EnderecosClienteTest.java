package com.gakki.store.e2e.casos;

import com.gakki.store.e2e.BaseE2ETest;
import com.gakki.store.e2e.acoes.FluxoDeCadastro;
import com.gakki.store.e2e.paginas.PaginaPerfil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RF0026 — Endereços do cliente, com as garantias da RN0021, RN0022 e
 * RN0023.
 *
 * <p>Cada teste começa cadastrando um cliente <b>novo</b> pela tela. É
 * mais lento do que reaproveitar o cliente do seed, e é de propósito:
 *
 * <ul>
 *   <li>o cliente nasce com exatamente um endereço, de entrega e de
 *       cobrança — o estado em que a RN0022 pode ser provada sem montar
 *       nada antes;</li>
 *   <li>nenhum teste enxerga o que outro deixou para trás, então a
 *       suíte dá o mesmo resultado rodando inteira, sozinha ou fora de
 *       ordem;</li>
 *   <li>o cliente de demonstração do seed continua intacto para a
 *       apresentação.</li>
 * </ul>
 */
@DisplayName("RF0026 — Endereços do cliente")
class RF0026EnderecosClienteTest extends BaseE2ETest {

    private static final String CASA = FluxoDeCadastro.APELIDO_DO_ENDERECO_INICIAL;
    private static final String TRABALHO = "Trabalho";

    @BeforeEach
    void cadastrarClienteEAbrirPerfil() {
        cadastro().registrarClienteNovo();

        abrir(PaginaPerfil.CAMINHO);
        esperarPor(PaginaPerfil.SECAO_ENDERECOS);

        // Ponto de partida de todos os testes: um endereço só.
        esperarQuantidade(PaginaPerfil.ENDERECOS, 1);
    }

    @Test
    @DisplayName("Cadastra um segundo endereço e ele passa a constar na lista")
    void cadastraSegundoEndereco() {
        adicionarEndereco(TRABALHO, true, false);

        esperarQuantidade(PaginaPerfil.ENDERECOS, 2);
        assertThat(navegador.findElements(PaginaPerfil.endereco(TRABALHO))).hasSize(1);
    }

    @Test
    @DisplayName("RN0023 — recusa endereço que não é de entrega nem de cobrança")
    void recusaEnderecoSemFinalidade() {
        adicionarEndereco(TRABALHO, false, false);

        assertThat(textoDe(PaginaPerfil.ERRO_ENDERECO)).containsIgnoringCase("ambos");

        // E nada foi gravado: a lista continua com o endereço do cadastro.
        assertThat(navegador.findElements(PaginaPerfil.ENDERECOS)).hasSize(1);
    }

    @Test
    @DisplayName("Altera um endereço existente e a lista reflete a mudança")
    void alteraEnderecoExistente() {
        adicionarEndereco(TRABALHO, true, false);
        esperarQuantidade(PaginaPerfil.ENDERECOS, 2);

        clicar(PaginaPerfil.acao(TRABALHO, "alterar"));
        substituir(PaginaPerfil.END_APELIDO, "Escritorio");
        substituir(PaginaPerfil.END_NUMERO, "2000");
        clicar(PaginaPerfil.BOTAO_SALVAR_ENDERECO);

        // Alteração, e não inclusão: continuam sendo dois endereços.
        esperarPor(PaginaPerfil.endereco("Escritorio"));
        assertThat(navegador.findElements(PaginaPerfil.ENDERECOS)).hasSize(2);
        assertThat(navegador.findElements(PaginaPerfil.endereco(TRABALHO))).isEmpty();
    }

    @Test
    @DisplayName("Define outro endereço como principal e o anterior deixa de ser")
    void defineOutroComoPrincipal() {
        adicionarEndereco(TRABALHO, true, false);
        esperarQuantidade(PaginaPerfil.ENDERECOS, 2);

        assertThat(atributoDe(PaginaPerfil.endereco(CASA), "data-principal")).isEqualTo("true");

        clicar(PaginaPerfil.acao(TRABALHO, "principal"));

        // O índice ux_endereco_principal não admite dois principais no
        // mesmo cliente: se a troca não desmarcasse o anterior, o backend
        // teria devolvido erro e nenhum dos dois mudaria.
        espera.until(nav -> "true".equals(
                nav.findElement(PaginaPerfil.endereco(TRABALHO)).getDomAttribute("data-principal")));
        assertThat(atributoDe(PaginaPerfil.endereco(CASA), "data-principal")).isEqualTo("false");
    }

    @Test
    @DisplayName("RN0022 — recusa remover o único endereço de entrega")
    void recusaRemoverUnicoEnderecoDeEntrega() {
        clicar(PaginaPerfil.acao(CASA, "remover"));

        assertThat(textoDe(PaginaPerfil.ERRO_ENDERECO)).containsIgnoringCase("único endereço de entrega");

        // O endereço continua lá. O 409 não é um aviso decorativo: a
        // operação foi recusada no servidor.
        assertThat(navegador.findElements(PaginaPerfil.endereco(CASA))).hasSize(1);
    }

    @Test
    @DisplayName("Remove um endereço acessório — exclusão física, diferente da inativação do cliente")
    void removeEnderecoAcessorio() {
        adicionarEndereco(TRABALHO, true, false);
        esperarQuantidade(PaginaPerfil.ENDERECOS, 2);

        clicar(PaginaPerfil.acao(TRABALHO, "remover"));

        // Endereço é dado acessório: sai do banco de verdade. O cadastro
        // do cliente, esse nunca é excluído — só inativado (RF0023).
        esperarQuantidade(PaginaPerfil.ENDERECOS, 1);
        assertThat(navegador.findElements(PaginaPerfil.endereco(TRABALHO))).isEmpty();
    }

    /**
     * Abre o formulário, preenche e salva.
     *
     * <p>Não espera pelo resultado de propósito: os casos de sucesso
     * esperam a lista crescer, e o caso da RN0023 espera a mensagem de
     * erro. Quem chama sabe o que vem depois.
     */
    private void adicionarEndereco(String apelido, boolean entrega, boolean cobranca) {
        clicar(PaginaPerfil.BOTAO_NOVO_ENDERECO);

        preencher(PaginaPerfil.END_APELIDO, apelido);
        selecionarPorValor(PaginaPerfil.END_TIPO_RESIDENCIA, "CASA");
        selecionarPorValor(PaginaPerfil.END_TIPO_LOGRADOURO, "AVENIDA");
        preencher(PaginaPerfil.END_LOGRADOURO, "Paulista");
        preencher(PaginaPerfil.END_NUMERO, "1000");
        preencher(PaginaPerfil.END_BAIRRO, "Bela Vista");
        preencher(PaginaPerfil.END_CIDADE, "Sao Paulo");
        preencher(PaginaPerfil.END_UF, "SP");
        preencher(PaginaPerfil.END_CEP, "01310100");
        preencher(PaginaPerfil.END_PAIS, "Brasil");

        // marcarCaixa, e não clicar: "entrega" já nasce marcada no
        // formulário e "cobrança" não, então um clique cego produziria
        // estados diferentes para os dois campos.
        marcarCaixa(PaginaPerfil.END_ENTREGA, entrega);
        marcarCaixa(PaginaPerfil.END_COBRANCA, cobranca);

        clicar(PaginaPerfil.BOTAO_SALVAR_ENDERECO);
    }
}
