package com.gakki.store.e2e.casos.vendas;

import com.gakki.store.e2e.BancoDeApoio;
import com.gakki.store.e2e.BaseVendasE2ETest;
import com.gakki.store.e2e.paginas.PaginaCheckout;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Formas de pagamento e seus limites — RF0037, RN0033, RN0034, RN0035.
 *
 * <p>O piso de R$ 10,00 por cartão (RN0034) e a exceção que o dispensa
 * quando há cupom no mesmo pagamento (RN0035) são as duas faces da
 * mesma regra, e por isso ficam juntas aqui.
 *
 * <p>A RN0033 entra como recusa: dois cupons promocionais na mesma
 * compra não passam.
 */
class RF0037PagamentoTest extends BaseVendasE2ETest {

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
}
