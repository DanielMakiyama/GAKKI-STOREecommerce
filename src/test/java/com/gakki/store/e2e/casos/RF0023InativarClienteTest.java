package com.gakki.store.e2e.casos;

import com.gakki.store.e2e.ApiDeApoio;
import com.gakki.store.e2e.BaseE2ETest;
import com.gakki.store.e2e.paginas.PaginaAdmin;
import com.gakki.store.e2e.paginas.PaginaLogin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RF0023 — Inativar cadastro de cliente.
 *
 * <p>É o caso que o enunciado destaca: "tratamento correto da distinção
 * entre inativação e exclusão". A prova tem duas metades, e nenhuma
 * delas basta sozinha:
 *
 * <ol>
 *   <li>Depois de inativado, o cadastro <b>continua</b> na listagem
 *       administrativa — não foi excluído.</li>
 *   <li>Mesmo assim, o cliente <b>não consegue mais entrar</b> — a
 *       inativação tem efeito.</li>
 * </ol>
 */
@DisplayName("RF0023 — Inativar cadastro de cliente")
class RF0023InativarClienteTest extends BaseE2ETest {

    private static final String EMAIL_CLIENTE = "cliente@gakkistore.com";

    private String tokenAdmin;
    private long idDoCliente;

    @BeforeEach
    void localizarClienteDeDemonstracao() {
        tokenAdmin = ApiDeApoio.tokenDoAdministrador();
        idDoCliente = ApiDeApoio.idDoCliente(tokenAdmin, EMAIL_CLIENTE);
    }

    /**
     * Devolve o cliente de demonstração ao estado ativo.
     *
     * <p>Roda pela API, e não pela tela, de propósito: se o teste falhar
     * no meio, a limpeza ainda acontece. Sem isso, uma falha deixaria o
     * cartão "Entrar como cliente" inutilizável para todas as outras
     * suítes.
     */
    @AfterEach
    void reativarCliente() {
        ApiDeApoio.ativar(tokenAdmin, idDoCliente);
    }

    @Test
    @DisplayName("Inativa pelo painel e o cadastro PERMANECE na listagem")
    void inativaSemExcluir() {
        abrirPainelComOCliente();

        assertThat(textoDe(PaginaAdmin.statusDoCliente(idDoCliente))).isEqualToIgnoringCase("Ativo");

        clicar(PaginaAdmin.botaoDeStatus(idDoCliente));

        // A MESMA linha continua na tabela, agora como inativa. É esta
        // asserção que separa inativar de excluir: numa exclusão, a
        // linha não existiria mais para ser verificada.
        esperarStatus("Inativo");

        assertThat(navegador.findElements(PaginaAdmin.linhaDoCliente(idDoCliente))).hasSize(1);
        assertThat(textoDe(PaginaAdmin.botaoDeStatus(idDoCliente))).isEqualToIgnoringCase("Ativar");
    }

    @Test
    @DisplayName("Cliente inativado não consegue autenticar")
    void inativadoNaoEntra() {
        // Estado preparado pela API: o que se quer testar aqui é o
        // login, não o caminho até o botão de inativar.
        ApiDeApoio.inativar(tokenAdmin, idDoCliente);

        abrir(PaginaLogin.CAMINHO);
        clicar(PaginaLogin.CARTAO_CLIENTE);

        // A senha está correta — o que barra é o estado do cadastro.
        assertThat(textoDe(PaginaLogin.ERRO)).containsIgnoringCase("inativo");
        assertThat(navegador.getCurrentUrl()).contains("/login");
    }

    @Test
    @DisplayName("Reativação pelo painel devolve o acesso ao cliente")
    void reativacaoDevolveAcesso() {
        ApiDeApoio.inativar(tokenAdmin, idDoCliente);

        abrirPainelComOCliente();
        assertThat(textoDe(PaginaAdmin.statusDoCliente(idDoCliente))).isEqualToIgnoringCase("Inativo");

        clicar(PaginaAdmin.botaoDeStatus(idDoCliente));
        esperarStatus("Ativo");

        // E o acesso volta: o cartão de cliente entra normalmente.
        abrir(PaginaLogin.CAMINHO);
        clicar(PaginaLogin.CARTAO_CLIENTE);
        esperarUrlConter("/catalogo");
    }

    /**
     * Espera o selo de status mudar.
     *
     * <p>Comparação insensível a maiúsculas porque o CSS do selo aplica
     * {@code text-transform: uppercase}, e o {@code getText()} do
     * Selenium devolve o texto como ele aparece na tela — "ATIVO", e não
     * "Ativo" como está no JSX.
     */
    private void esperarStatus(String esperado) {
        espera.until(nav ->
                esperado.equalsIgnoreCase(nav.findElement(PaginaAdmin.statusDoCliente(idDoCliente)).getText()));
    }

    /**
     * Entra no painel e garante que a linha do cliente está visível.
     *
     * <p>A entrada em si mora no {@code BaseE2ETest}, porque a RF0024
     * precisa dela também. O que é específico daqui é exigir a linha
     * deste cliente — sem ela, os testes falhariam com "elemento não
     * encontrado" em vez de dizer que o seed não foi aplicado.
     */
    private void abrirPainelComOCliente() {
        entrarComoAdministrador();

        try {
            esperarPor(PaginaAdmin.linhaDoCliente(idDoCliente));
        } catch (NoSuchElementException e) {
            throw new AssertionError(
                    "Cliente de demonstração não apareceu na listagem. O seed V999 foi aplicado?", e);
        }
    }
}
