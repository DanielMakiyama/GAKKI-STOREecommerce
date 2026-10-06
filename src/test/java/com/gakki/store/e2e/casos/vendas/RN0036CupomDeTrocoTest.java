package com.gakki.store.e2e.casos.vendas;

import com.gakki.store.e2e.ApiDeApoio;
import com.gakki.store.e2e.BancoDeApoio;
import com.gakki.store.e2e.BaseVendasE2ETest;
import com.gakki.store.e2e.paginas.PaginaCheckout;
import com.gakki.store.e2e.paginas.PaginaCupons;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Cupom de troco — RN0036, nas suas duas metades.
 *
 * <p>A regra manda emitir cupom pela diferença quando os cupons usados
 * superam o valor da compra, <b>e</b> proíbe o uso de cupons
 * desnecessários. Os dois casos usam os valores do exemplo numérico do
 * DRS: cupons de 20, 40 e 35 numa compra de R$ 50,00.
 *
 * <p>O algoritmo do conjunto mínimo não é definido pelo DRS — a regra
 * adotada ({@code soma − menor < valorCompra}) é dedução a partir do
 * exemplo, e está na decisão 4 do contrato.
 */
class RN0036CupomDeTrocoTest extends BaseVendasE2ETest {

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
}
