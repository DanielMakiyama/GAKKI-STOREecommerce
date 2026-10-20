package com.gakki.store.e2e.casos.vendas;

import com.gakki.store.e2e.ApiDeApoio;
import com.gakki.store.e2e.BancoDeApoio;
import com.gakki.store.e2e.BaseVendasE2ETest;
import com.gakki.store.e2e.paginas.PaginaAdminVendas;
import com.gakki.store.e2e.paginas.PaginaCupons;
import com.gakki.store.e2e.paginas.PaginaPedidos;
import com.gakki.store.e2e.paginas.PaginaTrocas;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Processo de troca, da solicitação ao cupom —
 * RF0041 a RF0045, RN0041 a RN0043, RN0046.
 *
 * <p>Os três estados são os do DRS: {@code EM_TROCA},
 * {@code TROCA_AUTORIZADA}, {@code TROCADO}. O protótipo trabalhava com
 * seis, e nenhum dos nomes antigos existe mais — nem na tela, nem no
 * enum, nem no CHECK da tabela.
 *
 * <p>Não há caso de recusa porque não há recusa: a RF0042 descreve só o
 * caminho da autorização, e o DRS não prevê estado nem regra para a
 * negativa. A lacuna está registrada na seção 16.2 da Especificação de
 * Caso de Uso — um caso aqui estaria testando requisito inventado.
 *
 * <p>Levar o pedido até ENTREGUE é preparo feito pela API: a RN0043 é a
 * porta de entrada da troca, e o despacho tem os seus próprios casos em
 * {@code RF0039DespachoEEntregaTest}, onde cada passo é dado pela tela.
 */
class RF0041TrocaTest extends BaseVendasE2ETest {

    @Test
    @DisplayName("RN0043 — só compra entregue oferece troca")
    void somenteCompraEntregueOfereceTroca() {
        String emProcessamento = compraPelaApi(PEDAL, 1);

        entrarComoCliente();
        irParaPedidos();
        esperarPor(PaginaPedidos.pedidoDeNumero(emProcessamento));

        assertTrue(navegador.findElements(PaginaPedidos.painelDeTroca(emProcessamento)).isEmpty(),
                "pedido em processamento não deveria oferecer troca (RN0043)");

        // Entregue, o mesmo pedido passa a oferecer.
        entregarPelaApi(emProcessamento);
        irParaPedidos();
        esperarPor(PaginaPedidos.painelDeTroca(emProcessamento));

        BancoDeApoio.reporEstoque(PEDAL, 1);
    }

    @Test
    @DisplayName("RF0041, RN0041 — cliente seleciona o item e o pedido de troca nasce EM_TROCA")
    void clienteSolicitaTrocaDosItensSelecionados() {
        String numeroDoPedido = pedidoEntregue(PEDAL, 1);
        long itemPedidoId = ApiDeApoio.idsDosItens(token, numeroDoPedido).get(0);

        entrarComoCliente();
        irParaPedidos();
        esperarPor(PaginaPedidos.painelDeTroca(numeroDoPedido));

        marcarCaixa(PaginaPedidos.caixaDoItem(numeroDoPedido, itemPedidoId), true);
        clicar(PaginaPedidos.botaoSolicitarTroca(numeroDoPedido));
        esperarPor(PaginaPedidos.MENSAGEM);

        // O número da troca vem da mensagem da tela: ele é gerado por
        // sequence, então fixá-lo no código funcionaria uma vez só.
        String numeroDaTroca = numeroNaMensagem(textoDe(PaginaPedidos.MENSAGEM));

        irParaTrocas();
        esperarPor(PaginaTrocas.trocaDeNumero(numeroDaTroca));

        assertEquals("EM_TROCA",
                atributoDe(PaginaTrocas.trocaDeNumero(numeroDaTroca), "data-status"),
                "a troca deveria nascer EM_TROCA (RN0041)");
        assertValor("700.00", reais(textoDe(PaginaTrocas.valorDaTroca(numeroDaTroca))),
                "valor da troca pelo preço da compra, não pelo preço de hoje");
        assertEquals("EM_TROCA", BancoDeApoio.statusDaTroca(numeroDaTroca),
                "status gravado, não só exibido");

        // RN0041 — a compra inteira em troca espelha o status no pedido.
        assertEquals("EM_TROCA", ApiDeApoio.statusDoPedido(token, numeroDoPedido),
                "o pedido tinha um item só, então a troca abrange a compra inteira");

        // O item já em troca não pode entrar noutra solicitação.
        irParaPedidos();
        esperarPor(PaginaPedidos.pedidoDeNumero(numeroDoPedido));
        assertTrue(navegador.findElements(PaginaPedidos.painelDeTroca(numeroDoPedido)).isEmpty(),
                "sem item trocável, o painel não deveria mais aparecer");

        BancoDeApoio.reporEstoque(PEDAL, 1);
    }

    @Test
    @DisplayName("RF0042, RN0046 — administrador autoriza e o cliente é notificado")
    void administradorAutorizaEClienteENotificado() {
        String numeroDaTroca = trocaSolicitada(PEDAL);
        int notificacoesAntes = ApiDeApoio.quantidadeDeNotificacoes(token);

        entrarComoAdministradorEm(PaginaAdminVendas.ABA_TROCAS);
        esperarPor(PaginaAdminVendas.trocaDeNumero(numeroDaTroca));

        clicar(PaginaAdminVendas.botaoAutorizar(numeroDaTroca));
        esperarAtributo(PaginaAdminVendas.trocaDeNumero(numeroDaTroca), "data-status", "TROCA_AUTORIZADA");

        assertEquals("TROCA_AUTORIZADA", BancoDeApoio.statusDaTroca(numeroDaTroca),
                "status gravado depois da autorização");
        assertEquals(notificacoesAntes + 1, ApiDeApoio.quantidadeDeNotificacoes(token),
                "a autorização deveria gerar uma notificação ao cliente (RN0046)");

        // A notificação chega à tela do cliente, com o número da troca.
        entrarComoCliente();
        irParaTrocas();
        esperarPor(PaginaTrocas.BLOCO_NOTIFICACOES);

        List<String> mensagens = navegador.findElements(PaginaTrocas.NOTIFICACOES)
                .stream().map(elemento -> elemento.getText()).toList();

        assertTrue(mensagens.stream().anyMatch(mensagem -> mensagem.contains(numeroDaTroca)),
                () -> "nenhuma notificação cita a troca " + numeroDaTroca + ": " + mensagens);

        BancoDeApoio.reporEstoque(PEDAL, 1);
    }

    /**
     * RF0044, RF0045, RN0042 — recebimento com retorno ao estoque.
     *
     * <p>É o caso que fecha o ciclo: estado {@code TROCADO}, item de
     * volta ao estoque e cupom de troca disponível ao cliente, no valor
     * que a compra tinha.
     */
    @Test
    @DisplayName("RF0044, RF0045 — recebimento devolve ao estoque e emite o cupom de troca")
    void recebimentoComRetornoAoEstoqueEmiteCupom() {
        String numeroDaTroca = trocaSolicitada(PEDAL);
        autorizarPelaApi(numeroDaTroca);

        int estoqueAntes = ApiDeApoio.estoqueDoInstrumento(PEDAL);
        List<String> cuponsAntes = ApiDeApoio.codigosDosCupons(token);

        entrarComoAdministradorEm(PaginaAdminVendas.ABA_TROCAS);
        esperarPor(PaginaAdminVendas.trocaDeNumero(numeroDaTroca));

        clicar(PaginaAdminVendas.botaoReceberComEstoque(numeroDaTroca));
        esperarAtributo(PaginaAdminVendas.trocaDeNumero(numeroDaTroca), "data-status", "TROCADO");

        assertEquals("TROCADO", BancoDeApoio.statusDaTroca(numeroDaTroca), "status gravado");
        assertEquals(Boolean.TRUE, BancoDeApoio.trocaRetornouAoEstoque(numeroDaTroca),
                "a decisão do administrador deveria estar gravada (RF0044)");
        assertEquals(estoqueAntes + 1, ApiDeApoio.estoqueDoInstrumento(PEDAL),
                "o item deveria ter voltado ao estoque (RN0042)");

        // RF0045 — o cupom existe, vale o da compra e é do cliente.
        List<String> cuponsNovos = cuponsNovos(cuponsAntes, ApiDeApoio.codigosDosCupons(token));
        assertEquals(1, cuponsNovos.size(),
                () -> "a conclusão deveria emitir exatamente um cupom, veio " + cuponsNovos);
        assertValor("700.00", BancoDeApoio.valorDoCupom(cuponsNovos.get(0)),
                "o cupom de troca vale o que o item valia na compra");
        assertFalse(BancoDeApoio.cupomFoiUtilizado(cuponsNovos.get(0)),
                "o cupom deveria nascer disponível");

        // E o cliente vê o cupom nas duas telas onde ele importa.
        entrarComoCliente();
        irParaTrocas();
        esperarPor(PaginaTrocas.cupomDaTroca(numeroDaTroca));
        assertTrue(textoDe(PaginaTrocas.cupomDaTroca(numeroDaTroca)).contains(cuponsNovos.get(0)),
                "o cupom deveria aparecer na troca concluída");

        abrir(PaginaCupons.CAMINHO);
        esperarPor(PaginaCupons.linhaDoCupom(cuponsNovos.get(0)));
    }

    @Test
    @DisplayName("RF0044 — recebimento sem retorno ao estoque emite o cupom do mesmo jeito")
    void recebimentoSemRetornoAoEstoqueTambemEmiteCupom() {
        String numeroDaTroca = trocaSolicitada(CORDAS);
        autorizarPelaApi(numeroDaTroca);

        int estoqueAntes = ApiDeApoio.estoqueDoInstrumento(CORDAS);
        List<String> cuponsAntes = ApiDeApoio.codigosDosCupons(token);

        entrarComoAdministradorEm(PaginaAdminVendas.ABA_TROCAS);
        esperarPor(PaginaAdminVendas.trocaDeNumero(numeroDaTroca));

        clicar(PaginaAdminVendas.botaoReceberSemEstoque(numeroDaTroca));
        esperarAtributo(PaginaAdminVendas.trocaDeNumero(numeroDaTroca), "data-status", "TROCADO");

        assertEquals(Boolean.FALSE, BancoDeApoio.trocaRetornouAoEstoque(numeroDaTroca),
                "o descarte deveria estar gravado (RF0044)");
        assertEquals(estoqueAntes, ApiDeApoio.estoqueDoInstrumento(CORDAS),
                "item descartado não volta ao estoque");

        // O crédito ao cliente não depende da decisão sobre o estoque: a
        // RF0045 garante o cupom nos dois caminhos.
        List<String> cuponsNovos = cuponsNovos(cuponsAntes, ApiDeApoio.codigosDosCupons(token));
        assertEquals(1, cuponsNovos.size(),
                () -> "o cupom deveria ser emitido também sem retorno ao estoque, veio " + cuponsNovos);
        assertValor("35.00", BancoDeApoio.valorDoCupom(cuponsNovos.get(0)),
                "cordas custam 35 na compra");

        BancoDeApoio.reporEstoque(CORDAS, 1);
    }

    // ------------------------------------------------------------
    // Preparo
    // ------------------------------------------------------------

    /** Compra entregue, um item, com a troca já solicitada pela API. */
    private String trocaSolicitada(String codigoDoInstrumento) {
        String numeroDoPedido = pedidoEntregue(codigoDoInstrumento, 1);
        long pedidoId = ApiDeApoio.idDoPedido(token, numeroDoPedido);
        long itemPedidoId = ApiDeApoio.idsDosItens(token, numeroDoPedido).get(0);

        return ApiDeApoio.solicitarTroca(token, pedidoId, itemPedidoId, 1);
    }

    private void autorizarPelaApi(String numeroDaTroca) {
        ApiDeApoio.autorizarTroca(ApiDeApoio.tokenDoAdministrador(),
                ApiDeApoio.idDaTroca(token, numeroDaTroca));
    }

    /** "Troca TRC-00007 solicitada! …" devolve "TRC-00007". */
    private static String numeroNaMensagem(String mensagem) {
        for (String palavra : mensagem.split("\\s+")) {
            if (palavra.startsWith("TRC-")) {
                return palavra;
            }
        }
        throw new IllegalStateException("Nenhum número de troca em: " + mensagem);
    }
}
