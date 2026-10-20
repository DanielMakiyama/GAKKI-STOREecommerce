package com.gakki.store.e2e.casos.vendas;

import com.gakki.store.e2e.ApiDeApoio;
import com.gakki.store.e2e.BancoDeApoio;
import com.gakki.store.e2e.BaseVendasE2ETest;
import com.gakki.store.e2e.paginas.PaginaCarrinho;
import com.gakki.store.e2e.paginas.PaginaCheckout;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Bloqueio temporário do item no carrinho e sua expiração —
 * RN0044, RN0045 e RNF0042.
 *
 * <p>O prazo é parametrizado ({@code gakki.carrinho.prazo-bloqueio}) e
 * vale 15 minutos na demonstração. Nenhum caso aqui espera o relógio:
 * o {@code BancoDeApoio} põe {@code bloqueado_ate} no passado, que é o
 * mesmo estado que o tempo produziria. Quem decide que o prazo venceu
 * continua sendo o backend, no acesso seguinte ao carrinho — o caminho
 * de produção roda inteiro, só o relógio é adiantado.
 *
 * <p>Nenhuma asserção fixa "15 minutos": o prazo é parâmetro, e um caso
 * que o afirmasse ficaria vermelho na demonstração, onde ele é reduzido
 * para alguns segundos justamente para a expiração ser visível.
 */
class RN0044BloqueioDeCarrinhoTest extends BaseVendasE2ETest {

    @Test
    @DisplayName("RN0044 — o carrinho mostra a contagem do prazo de reserva")
    void carrinhoMostraAContagemDoPrazo() {
        entrarComoCliente();
        adicionarAoCarrinho(PEDAL, 1);
        irParaCarrinho();

        esperarPor(PaginaCarrinho.CONTAGEM);
        String contagem = textoDe(PaginaCarrinho.CONTAGEM_VALOR);

        assertTrue(contagem.matches("\\d{2}:\\d{2}"),
                () -> "a contagem deveria estar em mm:ss, veio \"" + contagem + "\"");
        assertTrue(compraEstaHabilitada(), "com o prazo em curso, a compra fica liberada");
    }

    /**
     * A observação da própria RN0044: "o prazo parametrizado deve ser
     * relativo ao último item incluído no carrinho".
     */
    @Test
    @DisplayName("RN0044 — incluir item reinicia o prazo do carrinho")
    void incluirItemReiniciaOPrazo() {
        entrarComoCliente();
        adicionarAoCarrinho(PEDAL, 1);

        OffsetDateTime prazoDoPrimeiroItem = BancoDeApoio.prazoDoCarrinho();
        assertTrue(prazoDoPrimeiroItem != null, "o primeiro item deveria iniciar a contagem");

        adicionarAoCarrinho(CORDAS, 1);
        OffsetDateTime prazoDoSegundoItem = BancoDeApoio.prazoDoCarrinho();

        assertTrue(prazoDoSegundoItem.isAfter(prazoDoPrimeiroItem),
                () -> "o prazo deveria ter sido reiniciado: era " + prazoDoPrimeiroItem
                        + " e ficou " + prazoDoSegundoItem);
    }

    /**
     * RN0045 e RNF0042 — vencido o prazo, os itens saem do carrinho, são
     * apresentados ao cliente e a compra fica desabilitada.
     */
    @Test
    @DisplayName("RN0045, RNF0042 — prazo vencido retira os itens e desabilita a compra")
    void prazoVencidoRetiraOsItensEDesabilitaACompra() {
        entrarComoCliente();
        adicionarAoCarrinho(PEDAL, 2);
        adicionarAoCarrinho(CORDAS, 1);

        vencerPrazoEVerCarrinho();

        assertEquals(2, quantidadeDeItensRetirados(), "as duas linhas deveriam ter sido retiradas");
        assertEquals(0, quantidadeDeLinhasNoCarrinho(), "nenhum item ativo deveria restar");
        esperarPor(PaginaCarrinho.AVISO_RETIRADOS);
        esperarPor(PaginaCarrinho.SEM_ITENS_ATIVOS);

        // O coração da RNF0042: "a opção comprar deve ser desabilitada".
        assertFalse(compraEstaHabilitada(), "a compra deveria estar desabilitada");
        esperarPor(PaginaCarrinho.MOTIVO_BLOQUEIO);

        // E não é só a tela: o servidor também recusa.
        assertFalse(ApiDeApoio.carrinhoPodeComprar(token),
                "o servidor deveria recusar a compra, não só o botão");
        assertEquals(2, BancoDeApoio.itensRetirados(), "as linhas deveriam estar em item_retirado");
    }

    /**
     * A outra metade da RNF0042: "os itens deverão ser adicionados
     * novamente no carrinho".
     */
    @Test
    @DisplayName("RNF0042 — item retirado volta ao carrinho e a compra é reabilitada")
    void itemRetiradoVoltaAoCarrinho() {
        entrarComoCliente();
        adicionarAoCarrinho(PEDAL, 2);

        vencerPrazoEVerCarrinho();
        assertEquals(1, quantidadeDeItensRetirados(), "a linha do pedal deveria estar retirada");

        restaurarPrimeiroItemRetirado();

        assertEquals(1, quantidadeDeLinhasNoCarrinho(), "o item deveria ter voltado");
        assertValor("1400.00", totalDoCarrinho(), "pedal 700 x 2, com o preço que tinha ao entrar");
        assertTrue(compraEstaHabilitada(), "sem item retirado, a compra volta a ser liberada");

        // Voltar ao carrinho é inclusão de item: a RN0044 manda reiniciar
        // a contagem, e não devolver o resto de um prazo já vencido.
        esperarPor(PaginaCarrinho.CONTAGEM);
        assertEquals(0, BancoDeApoio.itensRetirados(), "a linha retirada deveria ter sido consumida");
    }

    /**
     * O caminho que faltava: desistir do item.
     *
     * <p>Sem ele o cliente poderia ficar preso — a compra é recusada
     * enquanto houver item retirado, e devolver ao carrinho pode ser
     * impossível se outra pessoa levou a última unidade no intervalo.
     */
    @Test
    @DisplayName("RNF0042 — descartar o item retirado também reabilita a compra")
    void descartarItemRetiradoReabilitaACompra() {
        entrarComoCliente();
        adicionarAoCarrinho(PEDAL, 1);
        adicionarAoCarrinho(CORDAS, 1);

        vencerPrazoEVerCarrinho();
        assertEquals(2, quantidadeDeItensRetirados(), "as duas linhas deveriam estar retiradas");

        restaurarPrimeiroItemRetirado();
        assertFalse(compraEstaHabilitada(), "com um item ainda retirado, a compra segue bloqueada");

        descartarPrimeiroItemRetirado();

        assertEquals(0, quantidadeDeItensRetirados(), "nada mais deveria estar retirado");
        assertTrue(compraEstaHabilitada(), "resolvidos os retirados, a compra é liberada");
        assertEquals(1, quantidadeDeLinhasNoCarrinho(), "só o item restaurado deveria estar no carrinho");
    }

    /**
     * RNF0042 na finalização: a regra não vive no JavaScript da tela.
     *
     * <p>Com item retirado pendente, quem chega ao checkout por URL é
     * mandado de volta ao carrinho — e, se insistisse, o
     * {@code POST /pedidos} recusaria.
     */
    @Test
    @DisplayName("RNF0042 — o checkout recusa quem tem item retirado pendente")
    void checkoutRecusaComItemRetiradoPendente() {
        entrarComoCliente();
        adicionarAoCarrinho(PEDAL, 1);

        vencerPrazoEVerCarrinho();

        abrir(PaginaCheckout.CAMINHO);
        esperarPor(PaginaCheckout.BLOQUEADO);
    }
}
