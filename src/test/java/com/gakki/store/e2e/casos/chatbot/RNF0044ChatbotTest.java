package com.gakki.store.e2e.casos.chatbot;

import com.gakki.store.e2e.BancoDeApoio;
import com.gakki.store.e2e.BaseE2ETest;
import com.gakki.store.e2e.paginas.PaginaChat;
import com.gakki.store.e2e.paginas.PaginaLogin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

// RNF0044 — chatbot de recomendação.
// Os casos de avaliação usam uma conversa criada no banco, com resposta pronta:
// não dependem da IA (lenta e variável) e são apagados no fim, para não
// alterar as conversas da massa de dados, que entram no ajuste fino.
@DisplayName("RNF0044 — chatbot de recomendação")
class RNF0044ChatbotTest extends BaseE2ETest {

    private static final String LUCAS = "lucas.ferreira@example.com";
    private static final By CARTAO_LUCAS = By.cssSelector("[data-perfil='lucas']");

    private static final String RESPOSTA_PRONTA =
            "Recomendo o Microfone Audio-Technica AT2020 [INST-0027], por R$ 700,00. É ótimo para gravar voz.";

    // A IA roda no processador: a primeira resposta pode passar de dois minutos.
    private static final Duration TEMPO_DA_IA = Duration.ofSeconds(240);

    private Long mensagemCriada;

    @AfterEach
    void apagarConversaDeTeste() {
        if (mensagemCriada != null) {
            BancoDeApoio.apagarConversaDaMensagem(mensagemCriada);
        }
    }

    @Test
    @DisplayName("Critério a: o chat não aparece para o administrador")
    void chatNaoApareceParaAdministrador() {
        entrarComoAdministrador();

        assertTrue(navegador.findElements(PaginaChat.BOTAO_ABRIR).isEmpty());
    }

    @Test
    @DisplayName("Critério d: o cliente avalia a resposta, troca a avaliação e as duas ficam registradas")
    void clienteAvaliaRespostaEAvaliacaoFicaRegistrada() {
        long id = conversaComRespostaPronta();
        entrarComoLucasEAbrirChat();
        esperarPor(PaginaChat.resposta(id));
        assertNull(BancoDeApoio.avaliacaoDaResposta(id));

        clicar(PaginaChat.botaoUtil(id));
        espera.until(ExpectedConditions.attributeToBe(PaginaChat.botaoUtil(id), "aria-pressed", "true"));
        assertEquals("UTIL", BancoDeApoio.avaliacaoDaResposta(id));
        assertFalse(BancoDeApoio.avaliacaoFoiAlterada(id));

        clicar(PaginaChat.botaoNaoUtil(id));
        espera.until(ExpectedConditions.attributeToBe(PaginaChat.botaoNaoUtil(id), "aria-pressed", "true"));
        assertEquals("NAO_UTIL", BancoDeApoio.avaliacaoDaResposta(id));
        assertTrue(BancoDeApoio.avaliacaoFoiAlterada(id));
        assertEquals("false", atributoDe(PaginaChat.botaoUtil(id), "aria-pressed"));
    }

    @Test
    @DisplayName("Cenário 4: o produto citado na resposta vira um link para a página dele")
    void produtoCitadoViraLinkParaAPagina() {
        long id = conversaComRespostaPronta();
        entrarComoLucasEAbrirChat();

        assertTrue(textoDe(PaginaChat.produtoCitado(id)).contains("AT2020"));
        clicar(PaginaChat.produtoCitado(id));
        esperarUrlConter("/produtos/");
    }

    // Usa a IA de verdade: precisa do Ollama aberto e demora. Aceita a resposta
    // ou o aviso de indisponibilidade, porque as duas saídas são corretas.
    @Test
    @Tag("ia")
    @DisplayName("Pergunta pela tela recebe a resposta da IA ou o aviso de indisponível")
    void perguntaRecebeRespostaOuAviso() {
        entrarComoLucasEAbrirChat();
        clicar(PaginaChat.NOVA_CONVERSA);
        esperarQuantidade(PaginaChat.RESPOSTAS, 0);

        preencher(PaginaChat.ENTRADA, "Qual pedal você me recomenda?");
        clicar(PaginaChat.BOTAO_ENVIAR);
        esperarPor(PaginaChat.PENSANDO);

        new WebDriverWait(navegador, TEMPO_DA_IA)
                .until(ExpectedConditions.invisibilityOfElementLocated(PaginaChat.PENSANDO));

        List<WebElement> respostas = navegador.findElements(PaginaChat.RESPOSTAS);
        if (respostas.isEmpty()) {
            assertTrue(textoDe(PaginaChat.AVISO).contains("indisponível"));
            return;
        }
        WebElement resposta = respostas.get(respostas.size() - 1);
        mensagemCriada = Long.parseLong(resposta.getDomAttribute("data-mensagem-id"));
        assertFalse(resposta.getText().isBlank());
    }

    private long conversaComRespostaPronta() {
        mensagemCriada = BancoDeApoio.criarConversaComResposta(LUCAS, "Qual microfone vocês têm?", RESPOSTA_PRONTA);
        return mensagemCriada;
    }

    private void entrarComoLucasEAbrirChat() {
        abrir(PaginaLogin.CAMINHO);
        clicar(CARTAO_LUCAS);
        esperarUrlConter("/catalogo");
        clicar(PaginaChat.BOTAO_ABRIR);
        esperarPor(PaginaChat.JANELA);
    }
}
