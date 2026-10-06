package com.gakki.store.e2e.casos.vendas;

import com.gakki.store.e2e.BaseVendasE2ETest;
import com.gakki.store.e2e.paginas.PaginaCheckout;
import com.gakki.store.e2e.paginas.PaginaPedidos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Finalização da compra — RF0038.
 *
 * <p>O pedido nasce com status {@code EM_PROCESSAMENTO}. O caso não se
 * contenta com a tela de confirmação, que exibe o objeto recém-criado
 * em memória: relê o pedido em "Meus pedidos", que vem do banco.
 */
class RF0038FinalizacaoTest extends BaseVendasE2ETest {

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
}
