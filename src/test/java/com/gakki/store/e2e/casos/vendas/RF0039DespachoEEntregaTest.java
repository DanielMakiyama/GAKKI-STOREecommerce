package com.gakki.store.e2e.casos.vendas;

import com.gakki.store.e2e.ApiDeApoio;
import com.gakki.store.e2e.BancoDeApoio;
import com.gakki.store.e2e.BaseVendasE2ETest;
import com.gakki.store.e2e.paginas.PaginaAdminVendas;
import com.gakki.store.e2e.paginas.PaginaPedidos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Acompanhamento, despacho e entrega pelo administrador —
 * RF0039, RF0040, RN0039 e RN0040.
 *
 * <p>Este é o caso que percorre o ciclo inteiro <b>pela tela do
 * administrador</b>, e não pela API: o painel é o que vai ser
 * demonstrado, e foi exatamente aqui que o protótipo quebrava — o mapa
 * de ações usava três status que não existem no sistema
 * ({@code EM_ABERTO}, {@code PAGAMENTO_REALIZADO}, {@code EM_TRANSITO}),
 * então o fluxo morria logo depois da confirmação do pagamento.
 *
 * <p>O status é lido do atributo {@code data-status} do cartão, e não do
 * texto da tag: {@code .status-tag} tem {@code text-transform: uppercase}
 * no CSS, então o texto renderizado não é o valor do enum.
 */
class RF0039DespachoEEntregaTest extends BaseVendasE2ETest {

    @Test
    @DisplayName("RF0039, RF0040 — administrador leva o pedido de EM_PROCESSAMENTO a ENTREGUE")
    void administradorPercorreOCicloDoPedido() {
        String numero = compraPelaApi(PEDAL, 1);

        entrarComoAdministradorEm(PaginaAdminVendas.ABA_PEDIDOS);
        esperarPor(PaginaAdminVendas.pedidoDeNumero(numero));

        assertEquals("EM_PROCESSAMENTO",
                atributoDe(PaginaAdminVendas.pedidoDeNumero(numero), "data-status"),
                "a compra deveria chegar ao painel aguardando a operadora");

        // RN0037 e RN0038 — o botão submete o pagamento à operadora.
        avancarEEsperar(numero, "APROVADA");

        // RF0039 — despacho.
        avancarEEsperar(numero, "EM_TRANSPORTE");

        // RF0040 — confirmação de entrega.
        avancarEEsperar(numero, "ENTREGUE");

        // Entregue, não há mais próxima ação do lado do pedido: a troca,
        // se houver, é conduzida na aba de trocas.
        assertTrue(navegador.findElements(PaginaAdminVendas.acaoDoPedido(numero)).isEmpty(),
                "pedido entregue não deveria oferecer mais nenhuma ação");

        // E o cliente vê o status final, relido do banco.
        entrarComoCliente();
        irParaPedidos();
        esperarPor(PaginaPedidos.pedidoDeNumero(numero));
        assertEquals("ENTREGUE", atributoDe(PaginaPedidos.pedidoDeNumero(numero), "data-status"),
                "status que chega ao cliente");

        BancoDeApoio.reporEstoque(PEDAL, 1);
    }

    @Test
    @DisplayName("RF0039 — o filtro por status isola a fila que o administrador quer ver")
    void filtroPorStatusIsolaAFila() {
        String emProcessamento = compraPelaApi(CORDAS, 1);
        String entregue = pedidoEntregue(PEDAL, 1);

        entrarComoAdministradorEm(PaginaAdminVendas.ABA_PEDIDOS);
        esperarPor(PaginaAdminVendas.pedidoDeNumero(emProcessamento));

        selecionarPorValor(PaginaAdminVendas.FILTRO_STATUS_PEDIDO, "EM_PROCESSAMENTO");
        esperarPor(PaginaAdminVendas.pedidoDeNumero(emProcessamento));
        assertTrue(navegador.findElements(PaginaAdminVendas.pedidoDeNumero(entregue)).isEmpty(),
                "o pedido entregue não deveria aparecer na fila de EM_PROCESSAMENTO");

        selecionarPorValor(PaginaAdminVendas.FILTRO_STATUS_PEDIDO, "ENTREGUE");
        esperarPor(PaginaAdminVendas.pedidoDeNumero(entregue));
        assertTrue(navegador.findElements(PaginaAdminVendas.pedidoDeNumero(emProcessamento)).isEmpty(),
                "o pedido em processamento não deveria aparecer na fila de ENTREGUE");

        BancoDeApoio.reporEstoque(PEDAL, 1);
    }

    /**
     * Clica na próxima ação do pedido e espera o status mudar.
     *
     * <p>Esperar o atributo, e não dormir: a tela recarrega a lista
     * depois do POST, e afirmar antes disso leria o status antigo.
     */
    private void avancarEEsperar(String numero, String statusEsperado) {
        clicar(PaginaAdminVendas.acaoDoPedido(numero));
        esperarAtributo(PaginaAdminVendas.pedidoDeNumero(numero), "data-status", statusEsperado);

        assertEquals(statusEsperado, ApiDeApoio.statusDoPedido(token, numero),
                "status gravado depois da ação, não só exibido");
    }
}
