package com.gakki.store.e2e.casos.vendas;

import com.gakki.store.e2e.ApiDeApoio;
import com.gakki.store.e2e.BancoDeApoio;
import com.gakki.store.e2e.BaseVendasE2ETest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Requisitos não funcionais do fluxo — RNF0011 e RNF0012.
 *
 * <p>Tempo de resposta das consultas e registro de log das operações de
 * escrita. Os dois estão nomeados no escopo do enunciado e nenhum dos
 * dois é visível na interface, então ambos são verificados nas suas
 * fontes: o relógio para o primeiro, a tabela {@code log_transacao}
 * para o segundo.
 */
class RequisitosNaoFuncionaisTest extends BaseVendasE2ETest {

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
}
