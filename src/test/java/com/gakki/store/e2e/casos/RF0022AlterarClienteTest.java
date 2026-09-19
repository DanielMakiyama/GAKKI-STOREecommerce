package com.gakki.store.e2e.casos;

import com.gakki.store.e2e.BaseE2ETest;
import com.gakki.store.e2e.GeradorDeDados;
import com.gakki.store.e2e.acoes.FluxoDeCadastro;
import com.gakki.store.e2e.paginas.PaginaPerfil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RF0022 — Alterar cadastro de cliente.
 *
 * <p>Toda alteração é conferida <b>depois de recarregar a página</b>. É
 * a diferença entre provar que gravou e provar que a tela mostrou o que
 * acabou de ser digitado: sem o recarregamento, um backend que
 * respondesse 200 e não persistisse nada passaria nos quatro testes.
 */
@DisplayName("RF0022 — Alterar cadastro de cliente")
class RF0022AlterarClienteTest extends BaseE2ETest {

    @Test
    @DisplayName("Altera nome e telefone, e a mudança persiste após recarregar")
    void alteraDadosEPersiste() {
        clienteNovoNoPerfil();

        substituir(PaginaPerfil.NOME, "Daniel Makiyama Alterado");
        selecionarPorValor(PaginaPerfil.TELEFONE_TIPO, "COMERCIAL");
        substituir(PaginaPerfil.TELEFONE_NUMERO, "977776666");
        clicar(PaginaPerfil.BOTAO_SALVAR_DADOS);

        esperarPor(PaginaPerfil.SUCESSO_DADOS);

        abrirPerfil();
        assertThat(valorDe(PaginaPerfil.NOME)).isEqualTo("Daniel Makiyama Alterado");
        assertThat(valorDe(PaginaPerfil.TELEFONE_TIPO)).isEqualTo("COMERCIAL");
        assertThat(valorDe(PaginaPerfil.TELEFONE_NUMERO)).isEqualTo("977776666");
    }

    /**
     * O caso que justifica o {@code CadastroAtualizadoResponse}.
     *
     * <p>O e-mail é o {@code subject} do JWT. No instante em que ele
     * muda, o token guardado no navegador aponta para um usuário que não
     * existe mais — e a requisição seguinte volta 401 logo depois de um
     * salvamento bem-sucedido. O backend devolve uma sessão nova junto
     * da resposta, e a tela a guarda antes de qualquer outra chamada.
     *
     * <p>Sair da página e voltar é o que exercita isso: se a sessão
     * tivesse morrido, a rota protegida jogaria o navegador no login.
     */
    @Test
    @DisplayName("Altera e-mail e CPF, e a sessão sobrevive à troca do e-mail")
    void alteraEmailECpfSemPerderASessao() {
        clienteNovoNoPerfil();

        String novoEmail = GeradorDeDados.emailUnico();
        String novoCpf = GeradorDeDados.cpfValido();

        substituir(PaginaPerfil.EMAIL, novoEmail);
        substituir(PaginaPerfil.CPF, novoCpf);
        clicar(PaginaPerfil.BOTAO_SALVAR_DADOS);

        esperarPor(PaginaPerfil.SUCESSO_DADOS);

        abrir("/catalogo");
        esperarUrlConter("/catalogo");

        abrirPerfil();
        assertThat(navegador.getCurrentUrl()).contains("/perfil");
        assertThat(valorDe(PaginaPerfil.EMAIL)).isEqualTo(novoEmail);
        assertThat(somenteDigitos(valorDe(PaginaPerfil.CPF))).isEqualTo(novoCpf);
    }

    @Test
    @DisplayName("RN0026 — recusa alteração com CPF inválido")
    void recusaCpfInvalido() {
        clienteNovoNoPerfil();
        String cpfOriginal = valorDe(PaginaPerfil.CPF);

        substituir(PaginaPerfil.CPF, GeradorDeDados.cpfInvalido());
        clicar(PaginaPerfil.BOTAO_SALVAR_DADOS);

        assertThat(textoDe(PaginaPerfil.ERRO_DADOS)).containsIgnoringCase("CPF inválido");

        // Nada foi gravado: o cadastro volta como estava.
        abrirPerfil();
        assertThat(valorDe(PaginaPerfil.CPF)).isEqualTo(cpfOriginal);
    }

    @Test
    @DisplayName("Recusa e-mail já usado por outro cadastro (409)")
    void recusaEmailDuplicado() {
        FluxoDeCadastro.Dados dados = clienteNovoNoPerfil();

        // O e-mail do cliente de demonstração, que vem do seed V999.
        substituir(PaginaPerfil.EMAIL, "cliente@gakkistore.com");
        clicar(PaginaPerfil.BOTAO_SALVAR_DADOS);

        assertThat(textoDe(PaginaPerfil.ERRO_DADOS)).containsIgnoringCase("e-mail");

        abrirPerfil();
        assertThat(valorDe(PaginaPerfil.EMAIL)).isEqualTo(dados.email());
    }

    private String somenteDigitos(String valor) {
        return valor == null ? "" : valor.replaceAll("\\D", "");
    }
}
