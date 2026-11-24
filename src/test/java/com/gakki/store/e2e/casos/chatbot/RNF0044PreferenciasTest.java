package com.gakki.store.e2e.casos.chatbot;

import com.gakki.store.e2e.BaseE2ETest;
import com.gakki.store.e2e.paginas.PaginaPreferencias;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// RNF0044, critério e — preferências obtidas por formulário no perfil.
// Cada caso usa um cliente recém-cadastrado: nenhum altera a massa de dados.
@DisplayName("RNF0044 — preferências musicais do cliente")
class RNF0044PreferenciasTest extends BaseE2ETest {

    @Test
    @DisplayName("Cliente informa as preferências e elas continuam salvas depois de recarregar")
    void salvaEPersistePreferencias() {
        clienteNovoNoPerfil();
        esperarPor(PaginaPreferencias.FORMULARIO);

        selecionarPorValor(PaginaPreferencias.NIVEL, "INTERMEDIARIO");
        preencher(PaginaPreferencias.ORCAMENTO, "1500");
        clicar(PaginaPreferencias.categoria("Pedais"));
        clicar(PaginaPreferencias.categoria("Guitarras"));
        clicar(PaginaPreferencias.estilo("ROCK"));
        clicar(PaginaPreferencias.estilo("BLUES"));
        preencher(PaginaPreferencias.OBSERVACOES, "Toco em uma banda aos sábados.");
        clicar(PaginaPreferencias.BOTAO_SALVAR);
        esperarPor(PaginaPreferencias.SUCESSO);

        navegador.navigate().refresh();
        // O formulário nasce com o nível padrão e é preenchido quando o GET responde.
        espera.until(nav -> "INTERMEDIARIO".equals(nav.findElement(PaginaPreferencias.NIVEL).getDomProperty("value")));

        assertEquals(1500.0, Double.parseDouble(valorDe(PaginaPreferencias.ORCAMENTO)), 0.001);
        assertEquals("Toco em uma banda aos sábados.", valorDe(PaginaPreferencias.OBSERVACOES));
        for (String categoria : List.of("Pedais", "Guitarras")) {
            assertEquals("true", atributoDe(PaginaPreferencias.categoria(categoria), "aria-pressed"), categoria);
        }
        for (String estilo : List.of("ROCK", "BLUES")) {
            assertEquals("true", atributoDe(PaginaPreferencias.estilo(estilo), "aria-pressed"), estilo);
        }
        assertEquals("false", atributoDe(PaginaPreferencias.estilo("JAZZ"), "aria-pressed"));
    }

    @Test
    @DisplayName("Sem nenhuma categoria marcada, o servidor recusa")
    void recusaSemCategoria() {
        clienteNovoNoPerfil();
        esperarPor(PaginaPreferencias.FORMULARIO);

        clicar(PaginaPreferencias.estilo("MPB"));
        clicar(PaginaPreferencias.BOTAO_SALVAR);

        assertTrue(textoDe(PaginaPreferencias.ERRO).contains("Escolha ao menos uma categoria."));
    }

    @Test
    @DisplayName("Mais de cinco estilos, o servidor recusa")
    void recusaMaisDeCincoEstilos() {
        clienteNovoNoPerfil();
        esperarPor(PaginaPreferencias.FORMULARIO);

        clicar(PaginaPreferencias.categoria("Violões"));
        for (String estilo : List.of("ROCK", "BLUES", "JAZZ", "POP", "MPB", "FUNK")) {
            clicar(PaginaPreferencias.estilo(estilo));
        }
        clicar(PaginaPreferencias.BOTAO_SALVAR);

        assertTrue(textoDe(PaginaPreferencias.ERRO).contains("Escolha no máximo 5 estilos musicais."));
    }
}
