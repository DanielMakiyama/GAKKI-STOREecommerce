package com.gakki.store.e2e.casos.vendas;

import com.gakki.store.e2e.ApiDeApoio;
import com.gakki.store.e2e.BaseVendasE2ETest;
import com.gakki.store.e2e.paginas.PaginaCheckout;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A compra, de ponta a ponta — RF0033, RF0034, RF0035 e RF0036.
 *
 * <p>Dois caminhos: com endereço e cartão já cadastrados no perfil, e
 * com os dois cadastrados durante a própria compra, respeitando a
 * composição da RN0023 e as bandeiras da RN0025.
 *
 * <p>"Incorporar ao perfil" (RF0035, RF0036) é verificado pela API, não
 * pela tela: a confirmação do pedido não mostra que o endereço e o
 * cartão entraram no cadastro do cliente.
 *
 * <p>O frete (RF0034) aparece nos dois casos — R$ 15,00 para destino em
 * SP e R$ 25,00 fora, que é a fórmula da decisão 10 do contrato.
 */
class RF0035EnderecoECartaoTest extends BaseVendasE2ETest {

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
}
