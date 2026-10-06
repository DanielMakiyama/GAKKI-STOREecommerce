package com.gakki.store.e2e;

import com.gakki.store.e2e.paginas.PaginaCarrinho;
import com.gakki.store.e2e.paginas.PaginaCatalogo;
import com.gakki.store.e2e.paginas.PaginaCheckout;
import com.gakki.store.e2e.paginas.PaginaLogin;
import com.gakki.store.e2e.paginas.PaginaProduto;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Passos compartilhados pelos casos de venda.
 *
 * <p>Estende o {@code BaseE2ETest} em vez de duplicá-lo: todos os
 * auxiliares de espera ({@code esperarPor}, {@code clicar},
 * {@code preencher}, {@code substituir}) vêm de lá e nada do módulo
 * Cliente precisou ser alterado.
 *
 * <p>Os passos aqui são sempre <b>pela tela</b>, porque é isso que o
 * enunciado exige demonstrar. Os atalhos por API e por banco
 * ({@code ApiDeApoio}, {@code BancoDeApoio}) só preparam e conferem
 * estado — nunca executam o fluxo sob teste.
 */
public abstract class BaseVendasE2ETest extends BaseE2ETest {

    // Códigos do seed V999, com os preços que as asserções dos casos
    // conferem: guitarra 1400, teclado 2800, pedal 700, interface 1260
    // (estoque 2), cordas 35, telecaster 1680 (estoque 0).
    //
    // O id é resolvido em tempo de execução: é BIGSERIAL e muda a cada
    // recriação do schema, então fixá-lo no código só adiaria uma quebra.
    protected static final String GUITARRA = "INST-0001";
    protected static final String TECLADO = "INST-0002";
    protected static final String PEDAL = "INST-0003";
    protected static final String INTERFACE_DE_AUDIO = "INST-0004";
    protected static final String CORDAS = "INST-0005";

    /** Estoque zero no seed — existe para a RN0031 poder ser demonstrada. */
    protected static final String ESGOTADO = "INST-0006";

    /** RNF0011 — "tempo de resposta das consultas". */
    protected static final long LIMITE_RNF0011_MS = 1000L;

    /**
     * Cartão de perfil do Login.jsx (atributo {@code data-perfil}).
     *
     * <p>O {@code PaginaLogin} já expõe o do administrador; o do cliente
     * fica aqui para esta fatia não mexer em arquivo do módulo Cliente.
     * Se {@code PaginaLogin.CARTAO_CLIENTE} for criado algum dia, trocar
     * por ele.
     */
    private static final By CARTAO_CLIENTE = By.cssSelector("[data-perfil='cliente']");

    protected String token;

    /**
     * Carrinho vazio antes de cada caso.
     *
     * <p>O carrinho é 1:1 com o cliente e persiste entre execuções: sem
     * essa limpeza, item esquecido por um caso anterior entraria na conta
     * do seguinte e o total deixaria de bater. Roda depois do
     * {@code @BeforeEach} da superclasse, que abre o navegador.
     */
    @BeforeEach
    void esvaziarCarrinho() {
        token = ApiDeApoio.tokenDoClienteDeDemonstracao();
        ApiDeApoio.limparCarrinho(token);
    }

    // ------------------------------------------------------------
    // Navegação
    // ------------------------------------------------------------

    protected void entrarComoCliente() {
        abrir(PaginaLogin.CAMINHO);
        clicar(CARTAO_CLIENTE);
        esperarUrlConter("/catalogo");
        esperarPor(PaginaCatalogo.GRADE);
    }

    /**
     * Abre o produto a partir da vitrine, como o cliente faz.
     *
     * <p>O card do catálogo não tem botão de adicionar — só o link do
     * título — então passar pela página do produto não é rodeio: é o
     * único caminho que a interface oferece.
     */
    protected long abrirProduto(String codigoDoInstrumento) {
        long instrumentoId = ApiDeApoio.idDoInstrumento(codigoDoInstrumento);

        abrir(PaginaCatalogo.CAMINHO);
        esperarPor(PaginaCatalogo.GRADE);
        clicar(PaginaCatalogo.linkDoProduto(instrumentoId));

        esperarUrlConter(PaginaProduto.caminho(instrumentoId));
        esperarPor(PaginaProduto.BOTAO_ADICIONAR);
        return instrumentoId;
    }

    protected void adicionarAoCarrinho(String codigoDoInstrumento, int quantidade) {
        abrirProduto(codigoDoInstrumento);

        if (quantidade > 1) {
            // O stepper limita ao estoque: pedir acima dele é recusado
            // pela própria tela, e não chega ao backend.
            substituir(PaginaProduto.QUANTIDADE, String.valueOf(quantidade));
        }

        clicar(PaginaProduto.BOTAO_ADICIONAR);

        // Esperar o aviso, e não dormir: o POST do carrinho acontece
        // entre o clique e a mensagem.
        esperarPor(PaginaProduto.AVISO_ADICIONADO);
    }

    protected void irParaCarrinho() {
        abrir(PaginaCarrinho.CAMINHO);
        esperarPor(PaginaCarrinho.TOTAL);
    }

    protected void irParaCheckout() {
        irParaCarrinho();
        clicar(PaginaCarrinho.BOTAO_FINALIZAR);
        esperarUrlConter("/checkout");
        esperarFreteCalculado();
    }

    // ------------------------------------------------------------
    // Carrinho
    // ------------------------------------------------------------

    protected int quantidadeDeLinhasNoCarrinho() {
        return navegador.findElements(PaginaCarrinho.ITENS).size();
    }

    /** Altera a quantidade da linha pela posição, contando de zero. */
    protected void alterarQuantidadeDaLinha(int indice, int quantidade) {
        WebElement campo = navegador.findElements(PaginaCarrinho.QUANTIDADES).get(indice);
        String totalAntes = textoDe(PaginaCarrinho.TOTAL);

        campo.sendKeys(Keys.chord(Keys.CONTROL, "a"), String.valueOf(quantidade));

        // O total do resumo só muda depois do PUT: esperar por ele é o
        // que garante que a próxima asserção lê o valor novo.
        espera.until(nav -> !nav.findElement(PaginaCarrinho.TOTAL).getText().equals(totalAntes));
    }

    protected void removerLinha(int indice) {
        int linhasAntes = quantidadeDeLinhasNoCarrinho();
        navegador.findElements(PaginaCarrinho.BOTOES_REMOVER).get(indice).click();
        esperarQuantidade(PaginaCarrinho.ITENS, linhasAntes - 1);
    }

    /**
     * Posição da linha do carrinho pelo nome do produto.
     *
     * <p>Contar na ordem em que o backend devolve os itens funcionaria
     * hoje, mas amarraria o teste a um detalhe que nenhum requisito
     * garante. Procurar pelo nome é o que o cliente faz.
     */
    protected int indiceDaLinha(String nomeParcial) {
        List<WebElement> nomes = navegador.findElements(PaginaCarrinho.NOMES);
        for (int i = 0; i < nomes.size(); i++) {
            if (nomes.get(i).getText().contains(nomeParcial)) {
                return i;
            }
        }
        throw new AssertionError("Nenhuma linha do carrinho com \"" + nomeParcial + "\"");
    }

    // ------------------------------------------------------------
    // Checkout — endereço
    // ------------------------------------------------------------

    /**
     * Cadastra endereço de entrega pelo próprio checkout (RF0035,
     * RN0023) e espera o frete ser recalculado para o destino novo.
     *
     * <p>O endereço nasce incorporado ao perfil do cliente — não existe
     * "usar só desta vez": o checkout chama o mesmo
     * {@code POST /clientes/me/enderecos} do módulo Cliente.
     */
    protected void cadastrarEnderecoDeEntrega(String apelido, String cidade, String uf, String cep) {
        String freteAntes = textoDe(PaginaCheckout.RESUMO_FRETE);

        clicar(PaginaCheckout.BOTAO_NOVO_ENDERECO);
        preencher(PaginaCheckout.ENDERECO_APELIDO, apelido);
        preencher(PaginaCheckout.ENDERECO_LOGRADOURO, "dos Testes Automatizados");
        preencher(PaginaCheckout.ENDERECO_NUMERO, "100");
        preencher(PaginaCheckout.ENDERECO_BAIRRO, "Centro");
        preencher(PaginaCheckout.ENDERECO_CIDADE, cidade);
        preencher(PaginaCheckout.ENDERECO_ESTADO, uf);
        preencher(PaginaCheckout.ENDERECO_CEP, cep);
        clicar(PaginaCheckout.BOTAO_SALVAR_ENDERECO);

        // O formulário fecha e o endereço novo já vem selecionado.
        esperarPor(PaginaCheckout.BOTAO_NOVO_ENDERECO);
        esperarFreteDiferenteDe(freteAntes);
    }

    protected int quantidadeDeEnderecosOferecidos() {
        return navegador.findElements(PaginaCheckout.OPCOES_DE_ENDERECO).size();
    }

    // ------------------------------------------------------------
    // Checkout — pagamento
    // ------------------------------------------------------------

    /**
     * Acrescenta linha de cartão deixando o valor que a tela sugere — o
     * que falta alocar para fechar o total.
     *
     * @param indiceDoCartao posição no {@code <select>}, não id: o id é
     *                       BIGSERIAL e muda a cada recriação do schema
     */
    protected void pagarRestanteNoCartao(int indiceDoCartao) {
        int linha = acrescentarLinhaDePagamento();
        selecionarCartaoDaLinha(linha, indiceDoCartao);
    }

    /** Acrescenta linha de cartão com valor explícito (RN0034, RN0035). */
    protected void pagarComCartao(int indiceDoCartao, String valor) {
        int linha = acrescentarLinhaDePagamento();
        selecionarCartaoDaLinha(linha, indiceDoCartao);

        WebElement campo = navegador.findElements(PaginaCheckout.VALOR_DA_LINHA).get(linha);
        campo.sendKeys(Keys.chord(Keys.CONTROL, "a"), valor);
    }

    /**
     * Acrescenta linha de cartão apontando para o cartão mais recente da
     * lista — o que acabou de ser cadastrado pelo checkout.
     *
     * <p>A posição é contada em tempo de execução em vez de fixada: o
     * cliente de demonstração tem dois cartões no seed, mas afirmar "é o
     * índice 2" quebraria se o seed ganhasse um terceiro.
     */
    protected void pagarRestanteNoUltimoCartao() {
        int linha = acrescentarLinhaDePagamento();
        WebElement seletor = navegador.findElements(PaginaCheckout.SELECT_DO_CARTAO).get(linha);
        Select select = new Select(seletor);
        select.selectByIndex(select.getOptions().size() - 1);
    }

    private int acrescentarLinhaDePagamento() {
        int linhasAntes = navegador.findElements(PaginaCheckout.LINHA_DE_PAGAMENTO).size();
        clicar(PaginaCheckout.BOTAO_USAR_CARTAO);
        esperarQuantidade(PaginaCheckout.LINHA_DE_PAGAMENTO, linhasAntes + 1);
        return linhasAntes;
    }

    private void selecionarCartaoDaLinha(int linha, int indiceDoCartao) {
        // A linha nova nasce com o primeiro cartão da lista; trocar é o
        // que permite dividir a compra entre dois cartões distintos.
        WebElement seletor = navegador.findElements(PaginaCheckout.SELECT_DO_CARTAO).get(linha);
        new Select(seletor).selectByIndex(indiceDoCartao);
    }

    /**
     * Cadastra cartão pelo checkout (RF0036, RN0024, RN0025).
     *
     * @param indiceDaBandeira posição no {@code <select>} de bandeiras,
     *                         contando o "Selecione…" como 0 — então a
     *                         primeira bandeira real é 1. Por posição, e
     *                         não por value, porque o value é o id no
     *                         banco e muda a cada recriação do schema.
     */
    protected void cadastrarCartao(String apelido, String ultimosDigitos, int indiceDaBandeira,
                                   String mes, String ano, String titular) {
        clicar(PaginaCheckout.BOTAO_CADASTRAR_CARTAO);
        preencher(PaginaCheckout.CARTAO_APELIDO, apelido);
        preencher(PaginaCheckout.CARTAO_DIGITOS, ultimosDigitos);
        selecionarPorIndice(PaginaCheckout.CARTAO_BANDEIRA, indiceDaBandeira);
        selecionarPorValor(PaginaCheckout.CARTAO_MES, mes);
        selecionarPorValor(PaginaCheckout.CARTAO_ANO, ano);
        preencher(PaginaCheckout.CARTAO_TITULAR, titular);
        clicar(PaginaCheckout.BOTAO_SALVAR_CARTAO);

        // O botão "Cadastrar cartão" fica visível o tempo todo, então
        // esperar por ele não prova nada: o sinal de que o cartão foi
        // aceito é o formulário embutido sumir.
        esperarQuantidade(PaginaCheckout.BOTAO_SALVAR_CARTAO, 0);
    }

    /** Ano de validade sempre no futuro, sem data fixa no código. */
    protected static String anoDeValidadeFuturo() {
        return String.valueOf(LocalDate.now().getYear() + 2);
    }

    // ------------------------------------------------------------
    // Checkout — cupons e confirmação
    // ------------------------------------------------------------

    protected void aplicarCupom(String codigo) {
        preencher(PaginaCheckout.CAMPO_CUPOM, codigo);
        clicar(PaginaCheckout.BOTAO_ADICIONAR_CUPOM);
        esperarPor(PaginaCheckout.tagDoCupom(codigo));
    }

    /** Tira um cupom da seleção clicando na própria tag (RN0036). */
    protected void removerCupom(String codigo) {
        clicar(PaginaCheckout.tagDoCupom(codigo));
        esperarQuantidade(PaginaCheckout.tagDoCupom(codigo), 0);
    }

    protected void confirmarPedido() {
        clicar(PaginaCheckout.BOTAO_CONFIRMAR);
        esperarPor(PaginaCheckout.CONFIRMACAO);
    }

    /** Confirma esperando recusa do backend, não a tela de sucesso. */
    protected String confirmarEsperandoErro() {
        clicar(PaginaCheckout.BOTAO_CONFIRMAR);
        return textoDe(PaginaCheckout.ERRO);
    }

    // ------------------------------------------------------------
    // Leitura de valores
    // ------------------------------------------------------------

    /**
     * O resumo só está íntegro depois que o frete real chega do backend:
     * antes disso o campo mostra "—" ou "calculando…", e comparar o
     * total ali é comparar com um número que ainda vai mudar.
     */
    protected void esperarFreteCalculado() {
        esperarPor(PaginaCheckout.RESUMO_FRETE);
        espera.until(nav -> nav.findElement(PaginaCheckout.RESUMO_FRETE).getText().contains("R$"));
    }

    protected void esperarFreteDiferenteDe(String freteAnterior) {
        espera.until(nav -> {
            String atual = nav.findElement(PaginaCheckout.RESUMO_FRETE).getText();
            return atual.contains("R$") && !atual.equals(freteAnterior);
        });
    }

    protected BigDecimal subtotalDoCheckout() {
        return reais(textoDe(PaginaCheckout.RESUMO_SUBTOTAL));
    }

    protected BigDecimal freteDoCheckout() {
        return reais(textoDe(PaginaCheckout.RESUMO_FRETE));
    }

    protected BigDecimal totalDoCheckout() {
        return reais(textoDe(PaginaCheckout.RESUMO_TOTAL));
    }

    protected BigDecimal totalDoCarrinho() {
        return reais(textoDe(PaginaCarrinho.TOTAL));
    }

    protected BigDecimal totalDoPedidoConfirmado() {
        return reais(textoDe(PaginaCheckout.PEDIDO_TOTAL));
    }

    /**
     * "R$ 1.415,00" para 1415.00.
     *
     * <p>Descarta tudo que não é dígito ou vírgula — o que elimina de uma
     * vez o "R$", o separador de milhar e o espaço estreito que o
     * {@code toLocaleString("pt-BR")} insere, que não é o espaço comum e
     * já quebrou comparação de string neste projeto.
     */
    protected static BigDecimal reais(String texto) {
        String numero = texto.replaceAll("[^0-9,]", "").replace(",", ".");
        if (numero.isEmpty()) {
            throw new IllegalArgumentException("Sem valor monetário em: " + texto);
        }
        return new BigDecimal(numero);
    }

    /** Códigos que apareceram em {@code depois} e não estavam em {@code antes}. */
    protected static List<String> cuponsNovos(List<String> antes, List<String> depois) {
        return depois.stream().filter(codigo -> !antes.contains(codigo)).toList();
    }

    // ------------------------------------------------------------
    // Asserções do domínio de vendas
    //
    // Ficam aqui, e não em cada classe de caso, porque as três carregam
    // uma decisão sobre COMO comparar — e essa decisão tem de ser a mesma
    // em toda a suíte.
    // ------------------------------------------------------------

    /**
     * Compara valores monetários por {@code compareTo}, não por
     * {@code equals}: {@code BigDecimal} considera 50 e 50.00 diferentes
     * porque compara a escala junto.
     */
    protected static void assertValor(String esperado, BigDecimal atual, String oQue) {
        assertEquals(0, new BigDecimal(esperado).compareTo(atual),
                () -> oQue + " — esperado R$ " + esperado + ", veio R$ " + atual);
    }

    /**
     * Compara texto de tela ignorando a caixa.
     *
     * <p>{@code getText()} devolve o texto <b>como renderizado</b>, e as
     * classes {@code .status-tag} e {@code .selo} do index.css têm
     * {@code text-transform: uppercase}: a tela mostra
     * "EM PROCESSAMENTO" onde o DOM guarda "Em processamento". Afirmar a
     * caixa amarraria o teste a uma decisão de estilo, que pode mudar
     * sem que nenhuma regra de negócio mude.
     */
    protected static void assertTexto(String esperado, String atual, String oQue) {
        assertTrue(esperado.equalsIgnoreCase(atual),
                () -> oQue + " — esperado \"" + esperado + "\", veio \"" + atual + "\"");
    }

    /** Limite do RNF0011: 1 segundo por consulta. */
    protected static void assertRapida(String rota, long milissegundos) {
        assertTrue(milissegundos < LIMITE_RNF0011_MS,
                () -> "GET " + rota + " levou " + milissegundos + " ms, acima do limite de "
                        + LIMITE_RNF0011_MS + " ms (RNF0011)");
    }
}
