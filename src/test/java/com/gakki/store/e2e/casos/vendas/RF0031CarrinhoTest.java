package com.gakki.store.e2e.casos.vendas;

import com.gakki.store.e2e.ApiDeApoio;
import com.gakki.store.e2e.BaseVendasE2ETest;
import com.gakki.store.e2e.paginas.PaginaCarrinho;
import com.gakki.store.e2e.paginas.PaginaCatalogo;
import com.gakki.store.e2e.paginas.PaginaProduto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Carrinho de compra — RF0031, RF0032 e RN0031.
 *
 * <p>Cobre adicionar, alterar quantidade, excluir e visualizar com mais
 * de um item, com a quantidade editada tanto na adição (no stepper da
 * página do produto) quanto na visualização do carrinho.
 *
 * <p>A RN0031 tem duas cláusulas, e cada uma tem o seu caso: item
 * indisponível em estoque e quantidade acima da disponível.
 */
class RF0031CarrinhoTest extends BaseVendasE2ETest {

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
}
