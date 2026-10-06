package com.gakki.store.e2e.casos;

import com.gakki.store.e2e.ApiDeApoio;
import com.gakki.store.e2e.BancoDeApoio;
import com.gakki.store.e2e.BaseVendasE2ETest;
import com.gakki.store.e2e.paginas.PaginaCarrinho;
import com.gakki.store.e2e.paginas.PaginaCatalogo;
import com.gakki.store.e2e.paginas.PaginaCheckout;
import com.gakki.store.e2e.paginas.PaginaCupons;
import com.gakki.store.e2e.paginas.PaginaPedidos;
import com.gakki.store.e2e.paginas.PaginaProduto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebElement;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Caminho feliz da criação de pedido, pela interface.
 *
 * <p>Cobre RF0031 a RF0038 e as regras RN0023 a RN0025 e RN0031 a
 * RN0036. Fora de escopo por exclusão do enunciado: validação de
 * pagamento (RN0037, RN0038), despacho (RF0039, RF0040), baixa de
 * estoque (RF0053, RN0028) e bloqueio de carrinho (RN0032).
 *
 * <p><b>Frete (RF0034):</b> a fórmula não é definida pelo DRS. A adotada
 * é R$ 15,00 + R$ 5,00 por peça além da primeira + R$ 10,00 quando o
 * estado não é SP, e "peça" é a soma das quantidades. Todas as contas
 * abaixo saem dela.
 *
 * <p><b>Repetibilidade:</b> cada caso começa com o carrinho vazio
 * ({@code @BeforeEach} da superclasse) e os casos que precisam de cupom
 * criam os seus próprios, com código único, por JDBC. Nenhum cenário
 * depende de cupom do seed, então a suíte roda verde quantas vezes for
 * executada contra o mesmo banco.
 */
class RF0038CriarPedidoTest extends BaseVendasE2ETest {

    // Preços do seed V999, conferidos na própria asserção de subtotal:
    // guitarra 1400, pedal 700, cordas 35, interface 1260 (estoque 2),
    // telecaster 1680 (estoque 0).

    /** RNF0011 — "tempo de resposta das consultas". */
    private static final long LIMITE_RNF0011_MS = 1000L;

    @Test
    @DisplayName("RF0031/RF0032 — vários itens no carrinho, com alteração de quantidade e exclusão")
    void adicionaMaisDeUmItemEAlteraQuantidade() {
        entrarComoCliente();

        // RF0031 — quantidade escolhida JÁ NA ADIÇÃO, no stepper da
        // página do produto. O escopo pede a edição nos dois momentos.
        adicionarAoCarrinho(CORDAS, 2);
        adicionarAoCarrinho(PEDAL, 1);

        irParaCarrinho();
        assertEquals(2, quantidadeDeLinhasNoCarrinho(), "o carrinho deve aceitar mais de um item");
        assertValor("770.00", totalDoCarrinho(), "2 x cordas 35 + pedal 700");

        // RF0032 — e agora a edição na própria visualização do carrinho
        alterarQuantidadeDaLinha(indiceDaLinha("Cordas"), 3);
        assertValor("805.00", totalDoCarrinho(), "pedal 700 + 3 x cordas 35");

        // RF0032 — excluir item
        removerLinha(indiceDaLinha("Pedal"));
        assertEquals(1, quantidadeDeLinhasNoCarrinho(), "a linha do pedal deve sair");
        assertValor("105.00", totalDoCarrinho(), "só as 3 cordas");
    }

    @Test
    @DisplayName("RF0033/RF0035/RF0036/RF0038 — compra com endereço e cartão já cadastrados")
    void compraComEnderecoECartaoExistentes() {
        entrarComoCliente();
        adicionarAoCarrinho(GUITARRA, 1);

        // RF0033 — a compra começa no botão do carrinho
        irParaCheckout();

        assertValor("1400.00", subtotalDoCheckout(), "guitarra");
        assertValor("15.00", freteDoCheckout(), "1 peça, destino em SP");
        assertValor("1415.00", totalDoCheckout(), "subtotal + frete");

        // RF0035 — o endereço principal já vem selecionado
        assertTrue(quantidadeDeEnderecosOferecidos() >= 1, "o cliente do seed tem endereços de entrega");

        // RF0036 — cartão existente cobrindo o total
        pagarRestanteNoCartao(0);
        confirmarPedido();

        // RF0038
        assertTexto("Em processamento", textoDe(PaginaCheckout.PEDIDO_STATUS), "status na confirmação");
        assertValor("1415.00", totalDoPedidoConfirmado(), "total do pedido criado");
        assertTrue(textoDe(PaginaCheckout.PEDIDO_NUMERO).startsWith("PED-"),
                "o número do pedido vem da sequence (RNF0035)");
    }

    @Test
    @DisplayName("RF0035/RN0023 e RF0036/RN0024/RN0025 — endereço e cartão novos, incorporados ao perfil")
    void compraComEnderecoECartaoNovosIncorporadosAoPerfil() {
        int enderecosAntes = ApiDeApoio.quantidadeDeEnderecos(token);
        int cartoesAntes = ApiDeApoio.quantidadeDeCartoes(token);

        entrarComoCliente();
        adicionarAoCarrinho(CORDAS, 1);
        irParaCheckout();

        assertValor("15.00", freteDoCheckout(), "destino inicial em SP");
        assertValor("50.00", totalDoCheckout(), "cordas 35 + frete 15");

        // RF0035/RN0023 — endereço novo, com a composição completa que a
        // regra exige. Fora de SP de propósito: é o que faz o frete subir.
        cadastrarEnderecoDeEntrega("Teste MG", "Belo Horizonte", "MG", "30110000");

        assertValor("25.00", freteDoCheckout(), "15 base + 10 por estar fora de SP");
        assertValor("60.00", totalDoCheckout(), "cordas 35 + frete 25");

        // RF0036/RN0024/RN0025 — cartão novo. A bandeira sai do <select>
        // alimentado por GET /bandeiras: índice 0 é o "Selecione…", então
        // 1 é a primeira bandeira registrada no sistema.
        cadastrarCartao("Cartão do teste", "1234", 1, "12", anoDeValidadeFuturo(), "CLIENTE DEMONSTRACAO");
        pagarRestanteNoUltimoCartao();

        confirmarPedido();
        assertValor("60.00", totalDoPedidoConfirmado(), "total com o frete de fora de SP");

        // "Incorporar ao perfil" é a parte da RF0035/RF0036 que a tela de
        // confirmação não mostra: os dois tiveram de nascer vinculados ao
        // cliente, não "só para esta compra".
        assertEquals(enderecosAntes + 1, ApiDeApoio.quantidadeDeEnderecos(token),
                "o endereço do checkout entra no cadastro do cliente");
        assertEquals(cartoesAntes + 1, ApiDeApoio.quantidadeDeCartoes(token),
                "o cartão do checkout entra no cadastro do cliente");
    }

    @Test
    @DisplayName("RN0034 — compra dividida entre dois cartões, cada linha acima de R$ 10,00")
    void pagamentoComMaisDeUmCartao() {
        entrarComoCliente();
        adicionarAoCarrinho(GUITARRA, 1);
        irParaCheckout();

        assertValor("1415.00", totalDoCheckout(), "guitarra 1400 + frete 15");

        pagarComCartao(0, "1000.00");
        pagarRestanteNoCartao(1);

        assertEquals(2, navegador.findElements(PaginaCheckout.LINHA_DE_PAGAMENTO).size(),
                "duas linhas de pagamento");
        assertValor("1415.00", reais(textoDe(PaginaCheckout.RESUMO_ALOCADO)),
                "1000 no primeiro cartão + 415 no segundo");
        assertTrue(navegador.findElements(PaginaCheckout.FALTA_ALOCAR).isEmpty(),
                "nada deve faltar alocar");

        confirmarPedido();
        assertValor("1415.00", totalDoPedidoConfirmado(), "total do pedido");
    }

    @Test
    @DisplayName("RN0035 — com cupom no pagamento, o cartão pode receber menos de R$ 10,00")
    void pagamentoComCartaoECupomAbaixoDeDezReais() {
        // Cupom próprio deste caso: os do seed são de uso único e
        // tornariam a suíte executável só uma vez por banco.
        String promocional = BancoDeApoio.criarCupomPromocional(new BigDecimal("45.00"));

        entrarComoCliente();
        adicionarAoCarrinho(CORDAS, 1);
        irParaCheckout();

        assertValor("50.00", totalDoCheckout(), "cordas 35 + frete 15");

        aplicarCupom(promocional);
        // R$ 5,00 no cartão: abaixo do piso da RN0034, permitido aqui
        // justamente porque existe cupom no mesmo pagamento.
        pagarComCartao(0, "5.00");

        confirmarPedido();
        assertValor("50.00", totalDoPedidoConfirmado(), "cupom 45 + cartão 5");

        assertTrue(BancoDeApoio.cupomFoiUtilizado(promocional),
                "o cupom sai de circulação depois de usado");
    }

    @Test
    @DisplayName("RN0036 — dois cupons que superam a compra geram cupom de troco pela diferença")
    void usoDeCuponsGeraCupomDeTrocoPelaDiferenca() {
        // Os valores do exemplo do DRS: 40 + 35 numa compra de 50.
        // Conjunto mínimo, porque 75 - 35 = 40 < 50 — usar também um
        // terceiro cupom seria recusado como desnecessário.
        String deQuarenta = BancoDeApoio.criarCupomDeTroca(new BigDecimal("40.00"));
        String deTrintaECinco = BancoDeApoio.criarCupomDeTroca(new BigDecimal("35.00"));

        List<String> cuponsAntes = ApiDeApoio.codigosDosCupons(token);

        entrarComoCliente();
        adicionarAoCarrinho(CORDAS, 1);
        irParaCheckout();

        assertValor("50.00", totalDoCheckout(), "cordas 35 + frete 15");

        aplicarCupom(deQuarenta);
        aplicarCupom(deTrintaECinco);
        assertEquals("2", textoDe(PaginaCheckout.RESUMO_CUPONS), "dois cupons aplicados");

        // Sem cartão: os cupons cobrem o total inteiro (RN0035).
        confirmarPedido();
        assertValor("50.00", totalDoPedidoConfirmado(), "o pedido vale o total, não a soma dos cupons");

        assertTrue(BancoDeApoio.cupomFoiUtilizado(deQuarenta), "cupom de 40 consumido");
        assertTrue(BancoDeApoio.cupomFoiUtilizado(deTrintaECinco), "cupom de 35 consumido");

        // O troco é identificado por diferença: o código vem da sequence
        // da V14 e incrementa a cada emissão, então afirmar "TROCA-1000"
        // só valeria na primeira execução.
        List<String> novos = cuponsNovos(cuponsAntes, ApiDeApoio.codigosDosCupons(token));
        assertEquals(1, novos.size(), "exatamente um cupom de troco emitido");

        String troco = novos.get(0);
        assertValor("25.00", BancoDeApoio.valorDoCupom(troco), "75 em cupons numa compra de 50");

        // A prova na interface: nenhuma tela de pedido mostra o troco.
        abrir(PaginaCupons.CAMINHO);
        esperarPor(PaginaCupons.linhaDoCupom(troco));
        assertValor("25.00", reais(textoDe(PaginaCupons.valorDoCupom(troco))),
                "o troco aparece em Meus cupons");
        assertTexto("Disponível", textoDe(PaginaCupons.statusDoCupom(troco)),
                "o cupom de troco nasce disponível para a próxima compra");
    }

    @Test
    @DisplayName("RF0038 — pedido finalizado fica EM_PROCESSAMENTO e assim persiste")
    void pedidoFinalizadoFicaEmProcessamento() {
        entrarComoCliente();
        adicionarAoCarrinho(PEDAL, 1);
        irParaCheckout();

        assertValor("715.00", totalDoCheckout(), "pedal 700 + frete 15");

        pagarRestanteNoCartao(0);
        confirmarPedido();

        String numero = textoDe(PaginaCheckout.PEDIDO_NUMERO);
        assertTexto("Em processamento", textoDe(PaginaCheckout.PEDIDO_STATUS), "status na confirmação");

        // A tela de confirmação mostra o que acabou de ser criado; "Meus
        // pedidos" relê do banco. Só a segunda prova que o status
        // sobreviveu ao commit.
        abrir(PaginaPedidos.CAMINHO);
        esperarPor(PaginaPedidos.pedidoDeNumero(numero));

        assertEquals("EM_PROCESSAMENTO",
                atributoDe(PaginaPedidos.pedidoDeNumero(numero), "data-status"),
                "status gravado, não só exibido");
        assertValor("715.00", reais(textoDe(PaginaPedidos.totalDoPedido(numero))),
                "total persistido");
    }

    @Test
    @DisplayName("RN0031 — o carrinho recusa quantidade acima do estoque")
    void naoPermiteQuantidadeAcimaDoEstoque() {
        int estoque = ApiDeApoio.estoqueDoInstrumento(INTERFACE_DE_AUDIO);
        assertEquals(2, estoque, "a interface de áudio tem estoque 2 no seed — o cenário depende disso");

        entrarComoCliente();
        adicionarAoCarrinho(INTERFACE_DE_AUDIO, estoque);

        irParaCarrinho();
        assertValor("2520.00", totalDoCarrinho(), "2 x interface 1260");

        // Segunda tentativa de 2 unidades: o total no carrinho passaria a
        // 4, acima do estoque. O stepper da tela limita cada adição ao
        // estoque, mas não sabe o que já está no carrinho — quem recusa é
        // o backend, e a mensagem chega à tela.
        abrirProduto(INTERFACE_DE_AUDIO);
        substituir(PaginaProduto.QUANTIDADE, String.valueOf(estoque));
        clicar(PaginaProduto.BOTAO_ADICIONAR);

        assertFalse(textoDe(PaginaProduto.ERRO).isBlank(), "a recusa precisa aparecer na tela");

        irParaCarrinho();
        assertValor("2520.00", totalDoCarrinho(), "nada foi acrescentado na tentativa recusada");
    }

    @Test
    @DisplayName("RN0031 — a vitrine não deixa adicionar item indisponível em estoque")
    void naoPermiteAdicionarItemEsgotado() {
        assertEquals(0, ApiDeApoio.estoqueDoInstrumento(ESGOTADO),
                "o INST-0006 existe no seed com estoque zero — o cenário depende disso");

        long id = ApiDeApoio.idDoInstrumento(ESGOTADO);
        entrarComoCliente();

        // Primeira barreira: o selo na vitrine.
        abrir(PaginaCatalogo.CAMINHO);
        esperarPor(PaginaCatalogo.GRADE);
        // assertTexto, não assertEquals: a classe .selo também tem
        // text-transform: uppercase, e getText() devolve o renderizado.
        assertTexto("Esgotado", textoDe(PaginaCatalogo.seloEsgotadoDoProduto(id)),
                "o card avisa que o produto está esgotado");

        // Segunda barreira: a página do produto não oferece o botão.
        clicar(PaginaCatalogo.linkDoProduto(id));
        esperarUrlConter(PaginaProduto.caminho(id));

        assertFalse(esperarPor(PaginaProduto.BOTAO_ADICIONAR).isEnabled(),
                "o botão de adicionar nasce desabilitado sem estoque");
        assertTrue(textoDe(PaginaProduto.ESTOQUE).toLowerCase().contains("sem estoque"),
                "a indisponibilidade é dita em palavras, não só pelo botão apagado");

        // E o carrinho continua vazio: não existe caminho pela interface.
        abrir(PaginaCarrinho.CAMINHO);
        esperarPor(PaginaCarrinho.VAZIO);
    }

    @Test
    @DisplayName("RN0033 — a compra recusa mais de um cupom promocional")
    void apenasUmCupomPromocionalPorCompra() {
        String primeiro = BancoDeApoio.criarCupomPromocional(new BigDecimal("20.00"));
        String segundo = BancoDeApoio.criarCupomPromocional(new BigDecimal("20.00"));

        entrarComoCliente();
        adicionarAoCarrinho(CORDAS, 1);
        irParaCheckout();

        aplicarCupom(primeiro);
        aplicarCupom(segundo);
        pagarRestanteNoCartao(0);

        String erro = confirmarEsperandoErro();
        assertFalse(erro.isBlank(), "a recusa precisa aparecer na tela");

        // A prova de que o pedido não passou: cupom é marcado como
        // utilizado dentro da mesma transação do pedido. Se nenhum dos
        // dois foi consumido, nada foi gravado.
        assertFalse(BancoDeApoio.cupomFoiUtilizado(primeiro), "nenhum cupom pode ser consumido");
        assertFalse(BancoDeApoio.cupomFoiUtilizado(segundo), "nenhum cupom pode ser consumido");
        assertTrue(navegador.findElements(PaginaCheckout.CONFIRMACAO).isEmpty(),
                "a tela de confirmação não deve aparecer");
    }

    @Test
    @DisplayName("RN0036 — três cupons numa compra de R$ 50 é uso desnecessário e é recusado")
    void recusaCupomDesnecessario() {
        // Os três valores do exemplo do DRS. 20 + 40 + 35 = 95, e
        // 95 - 20 = 75 >= 50: o conjunto não é mínimo, porque dois
        // cupons quaisquer já cobrem a compra.
        String deVinte = BancoDeApoio.criarCupomDeTroca(new BigDecimal("20.00"));
        String deQuarenta = BancoDeApoio.criarCupomDeTroca(new BigDecimal("40.00"));
        String deTrintaECinco = BancoDeApoio.criarCupomDeTroca(new BigDecimal("35.00"));

        entrarComoCliente();
        adicionarAoCarrinho(CORDAS, 1);
        irParaCheckout();
        assertValor("50.00", totalDoCheckout(), "cordas 35 + frete 15");

        aplicarCupom(deVinte);
        aplicarCupom(deQuarenta);
        aplicarCupom(deTrintaECinco);

        String erro = confirmarEsperandoErro();
        assertTrue(erro.toLowerCase().contains("desnecess"),
                () -> "a mensagem deve explicar o motivo; veio: " + erro);
        assertFalse(BancoDeApoio.cupomFoiUtilizado(deVinte), "nada consumido na recusa");

        // Tirando o menor, o conjunto fica mínimo e a mesma compra passa:
        // 40 + 35 = 75, e 75 - 35 = 40 < 50.
        removerCupom(deVinte);
        confirmarPedido();

        assertValor("50.00", totalDoPedidoConfirmado(), "o pedido vale o total da compra");
        assertFalse(BancoDeApoio.cupomFoiUtilizado(deVinte),
                "o cupom retirado continua disponível para outra compra");
        assertTrue(BancoDeApoio.cupomFoiUtilizado(deQuarenta), "os dois mínimos foram consumidos");
        assertTrue(BancoDeApoio.cupomFoiUtilizado(deTrintaECinco), "os dois mínimos foram consumidos");
    }

    @Test
    @DisplayName("RNF0012 — a finalização grava log de transação com usuário, data e dados")
    void escritasDoPedidoGeramLogDeTransacao() {
        int pedidosAntes = BancoDeApoio.linhasDeLog("Pedido");
        int pagamentosAntes = BancoDeApoio.linhasDeLog("Pagamento");
        int itensAntes = BancoDeApoio.linhasDeLog("ItemPedido");

        entrarComoCliente();
        adicionarAoCarrinho(PEDAL, 1);
        irParaCheckout();
        pagarRestanteNoCartao(0);
        confirmarPedido();

        assertTrue(BancoDeApoio.linhasDeLog("Pedido") > pedidosAntes, "o pedido foi registrado no log");
        assertTrue(BancoDeApoio.linhasDeLog("ItemPedido") > itensAntes, "o item do pedido também");
        assertTrue(BancoDeApoio.linhasDeLog("Pagamento") > pagamentosAntes, "e a linha de pagamento");

        // A RN exige data, hora, usuário responsável e os dados
        // alterados — contar linhas não provaria nenhum dos quatro.
        BancoDeApoio.Log log = BancoDeApoio.ultimoLog("Pedido");
        assertEquals("INSERCAO", log.operacao(), "criação de pedido é inserção");
        assertEquals("cliente@gakkistore.com", log.usuarioEmail(), "usuário responsável");
        assertNotNull(log.criadoEm(), "data e hora");
        assertTrue(log.dadosAlterados().contains("EM_PROCESSAMENTO"),
                () -> "os dados gravados devem conter o estado resultante; veio: " + log.dadosAlterados());
    }

    @Test
    @DisplayName("RNF0011 — as consultas do fluxo respondem em menos de 1 segundo")
    void consultasRespondemEmMenosDeUmSegundo() {
        // Uma rodada de aquecimento primeiro: a primeira chamada paga
        // abertura de conexão, JIT e o primeiro plano de consulta do
        // PostgreSQL. Medir isso seria medir o start-up, não o sistema.
        ApiDeApoio.duracaoDeConsulta("/instrumentos?size=100", null);
        ApiDeApoio.duracaoDeConsulta("/pedidos", token);

        assertRapida("/instrumentos?size=100", ApiDeApoio.duracaoDeConsulta("/instrumentos?size=100", null));
        assertRapida("/pedidos", ApiDeApoio.duracaoDeConsulta("/pedidos", token));
        assertRapida("/cupons", ApiDeApoio.duracaoDeConsulta("/cupons", token));
        assertRapida("/carrinho", ApiDeApoio.duracaoDeConsulta("/carrinho", token));
        assertRapida("/clientes/me/enderecos",
                ApiDeApoio.duracaoDeConsulta("/clientes/me/enderecos", token));
    }

    // ------------------------------------------------------------
    // Auxiliares deste caso
    // ------------------------------------------------------------

    /**
     * Posição da linha do carrinho pelo nome do produto.
     *
     * <p>Contar na ordem em que o backend devolve os itens funcionaria
     * hoje, mas amarraria o teste a um detalhe que nenhum requisito
     * garante. Procurar pelo nome é o que o cliente faz.
     */
    private int indiceDaLinha(String nomeParcial) {
        List<WebElement> nomes = navegador.findElements(PaginaCarrinho.NOMES);
        for (int i = 0; i < nomes.size(); i++) {
            if (nomes.get(i).getText().contains(nomeParcial)) {
                return i;
            }
        }
        throw new AssertionError("Nenhuma linha do carrinho com \"" + nomeParcial + "\"");
    }

    /**
     * Compara texto de tela ignorando a caixa.
     *
     * <p>{@code getText()} devolve o texto <b>como renderizado</b>, e a
     * classe {@code .status-tag} do index.css tem
     * {@code text-transform: uppercase}: a tela mostra
     * "EM PROCESSAMENTO" onde o DOM guarda "Em processamento". Afirmar
     * a caixa amarraria o teste a uma decisão de estilo, que pode mudar
     * sem que nenhuma regra de negócio mude.
     */
    private static void assertTexto(String esperado, String atual, String oQue) {
        assertTrue(esperado.equalsIgnoreCase(atual),
                () -> oQue + " — esperado \"" + esperado + "\", veio \"" + atual + "\"");
    }

    /**
     * Compara valores monetários por {@code compareTo}, não por
     * {@code equals}: {@code BigDecimal} considera 50 e 50.00 diferentes
     * porque compara a escala junto.
     */
    private static void assertValor(String esperado, BigDecimal atual, String oQue) {
        assertEquals(0, new BigDecimal(esperado).compareTo(atual),
                () -> oQue + " — esperado R$ " + esperado + ", veio R$ " + atual);
    }

    /** Limite do RNF0011: 1 segundo por consulta. */
    private static void assertRapida(String rota, long milissegundos) {
        assertTrue(milissegundos < LIMITE_RNF0011_MS,
                () -> "GET " + rota + " levou " + milissegundos + " ms, acima do limite de "
                        + LIMITE_RNF0011_MS + " ms (RNF0011)");
    }
}
