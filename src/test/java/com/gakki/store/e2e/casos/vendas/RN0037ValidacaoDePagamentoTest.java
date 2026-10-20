package com.gakki.store.e2e.casos.vendas;

import com.gakki.store.e2e.ApiDeApoio;
import com.gakki.store.e2e.BancoDeApoio;
import com.gakki.store.e2e.BaseVendasE2ETest;
import com.gakki.store.e2e.paginas.PaginaCheckout;
import com.gakki.store.e2e.paginas.PaginaPedidos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Validação do pagamento e baixa em estoque —
 * RN0037, RN0038, RF0053 e RN0028.
 *
 * <p>A operadora de cartão é simulada, como o próprio DRS autoriza em
 * nota de rodapé. O critério de recusa é o final do cartão, parametrizado
 * em {@code gakki.pagamento.digitos-recusados} (0000 por padrão):
 * determinístico, e acionável pela tela, porque é o cliente quem cadastra
 * o cartão.
 *
 * <p>A baixa em estoque é consequência da aprovação, não da finalização
 * (RN0028): entre finalizar e a operadora responder a mercadoria fica
 * bloqueada, mas o estoque só é debitado quando o pagamento é aprovado.
 * Os dois casos abaixo são o par que prova isso — um aprovado que debita
 * e um reprovado que não.
 */
class RN0037ValidacaoDePagamentoTest extends BaseVendasE2ETest {

    /** O final que o {@code OperadoraDeCartao} recusa, por parâmetro. */
    private static final String FINAL_RECUSADO = "0000";

    @Test
    @DisplayName("RN0037, RN0038, RN0028 — pagamento aprovado dá baixa no estoque")
    void pagamentoAprovadoDaBaixaNoEstoque() {
        int estoqueAntes = ApiDeApoio.estoqueDoInstrumento(PEDAL);

        entrarComoCliente();
        adicionarAoCarrinho(PEDAL, 2);
        irParaCheckout();
        pagarRestanteNoCartao(0);
        confirmarPedido();

        String numero = textoDe(PaginaCheckout.PEDIDO_NUMERO);

        // Antes da operadora responder: ainda EM_PROCESSAMENTO e estoque
        // intacto. É este o estado em que a mercadoria está bloqueada sem
        // ter sido debitada.
        assertEquals("EM_PROCESSAMENTO", ApiDeApoio.statusDoPedido(token, numero),
                "a compra nasce aguardando a operadora (RF0038)");
        assertEquals(estoqueAntes, ApiDeApoio.estoqueDoInstrumento(PEDAL),
                "nada deveria ter sido debitado antes da aprovação (RN0028)");

        ApiDeApoio.confirmarPagamento(ApiDeApoio.tokenDoAdministrador(),
                ApiDeApoio.idDoPedido(token, numero));

        assertEquals("APROVADA", ApiDeApoio.statusDoPedido(token, numero),
                "cartão válido deveria ser aprovado (RN0038)");
        assertEquals(estoqueAntes - 2, ApiDeApoio.estoqueDoInstrumento(PEDAL),
                "a aprovação deveria debitar as 2 unidades (RF0053, RN0028)");

        // E o cliente vê o status novo, relido do banco.
        irParaPedidos();
        esperarPor(PaginaPedidos.pedidoDeNumero(numero));
        assertEquals("APROVADA", atributoDe(PaginaPedidos.pedidoDeNumero(numero), "data-status"),
                "status exibido ao cliente");

        // Devolve o que o caso consumiu, senão os 8 pedais do seed
        // acabam na quinta execução da suíte.
        BancoDeApoio.reporEstoque(PEDAL, 2);
    }

    @Test
    @DisplayName("RN0037, RN0038 — cartão recusado reprova a compra e não toca no estoque")
    void cartaoRecusadoReprovaACompra() {
        int estoqueAntes = ApiDeApoio.estoqueDoInstrumento(CORDAS);

        entrarComoCliente();
        adicionarAoCarrinho(CORDAS, 1);
        irParaCheckout();

        // Cartão cadastrado pela tela, com o final que a operadora
        // recusa. A primeira bandeira real é o índice 1 — o 0 é o
        // "Selecione…".
        cadastrarCartao("Cartão recusado E2E", FINAL_RECUSADO, 1,
                "12", anoDeValidadeFuturo(), "DANIEL M PEREIRA");
        pagarRestanteNoUltimoCartao();
        confirmarPedido();

        String numero = textoDe(PaginaCheckout.PEDIDO_NUMERO);

        ApiDeApoio.confirmarPagamento(ApiDeApoio.tokenDoAdministrador(),
                ApiDeApoio.idDoPedido(token, numero));

        assertEquals("REPROVADA", ApiDeApoio.statusDoPedido(token, numero),
                "o final " + FINAL_RECUSADO + " deveria ser recusado pela operadora (RN0037)");
        assertEquals(estoqueAntes, ApiDeApoio.estoqueDoInstrumento(CORDAS),
                "compra reprovada não debita estoque (RN0028)");

        irParaPedidos();
        esperarPor(PaginaPedidos.pedidoDeNumero(numero));
        assertEquals("REPROVADA", atributoDe(PaginaPedidos.pedidoDeNumero(numero), "data-status"),
                "status exibido ao cliente");
    }
}
