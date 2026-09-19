package com.gakki.store.e2e.casos;

import com.gakki.store.e2e.ApiDeApoio;
import com.gakki.store.e2e.BaseE2ETest;
import com.gakki.store.e2e.GeradorDeDados;
import com.gakki.store.e2e.acoes.FluxoDeCadastro;
import com.gakki.store.e2e.paginas.PaginaPerfil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * RF0028 — Alterar apenas a senha.
 *
 * <p>O requisito é literal: a senha deve poder ser trocada "sem que seja
 * necessária a alteração de todos os dados cadastrais". Por isso o
 * formulário de senha é separado do de dados na tela, e o endpoint é um
 * {@code PATCH} próprio, e não o {@code PUT} do cadastro.
 *
 * <p>Os três casos de recusa vêm todos do servidor. A tela não confere
 * nada antes de enviar, de propósito: uma validação em JavaScript
 * esconderia uma falha do backend, e o teste passaria provando o
 * navegador em vez da regra.
 */
@DisplayName("RF0028 — Alterar apenas a senha")
class RF0028AlterarSenhaTest extends BaseE2ETest {

    private static final String SENHA_NOVA = "Gakki@2027";

    @Test
    @DisplayName("Altera a senha, e a nova passa a valer no lugar da antiga")
    void alteraSenhaEANovaPassaAValer() {
        FluxoDeCadastro.Dados dados = clienteNovoNoPerfil();

        trocarSenha(dados.senha(), SENHA_NOVA, SENHA_NOVA);
        esperarPor(PaginaPerfil.SUCESSO_SENHA);

        // A tela diz que alterou. Quem prova que alterou de fato é uma
        // autenticação nova — e ela não pode ser feita pela interface,
        // porque a tela de entrada só oferece os dois perfis de
        // demonstração, sem campo de e-mail e senha.
        assertThatCode(() -> ApiDeApoio.token(dados.email(), SENHA_NOVA))
                .doesNotThrowAnyException();

        // E a antiga deixou de valer: a troca substituiu, não acrescentou.
        assertThatThrownBy(() -> ApiDeApoio.token(dados.email(), dados.senha()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Recusa a troca quando a senha atual está incorreta (401)")
    void recusaSenhaAtualIncorreta() {
        FluxoDeCadastro.Dados dados = clienteNovoNoPerfil();

        trocarSenha("Errada@2026", SENHA_NOVA, SENHA_NOVA);

        assertThat(textoDe(PaginaPerfil.ERRO_SENHA)).containsIgnoringCase("senha atual");

        // A senha original continua valendo: nada foi trocado.
        assertThatCode(() -> ApiDeApoio.token(dados.email(), dados.senha()))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("RNF0032 — recusa confirmação divergente da nova senha")
    void recusaConfirmacaoDivergente() {
        FluxoDeCadastro.Dados dados = clienteNovoNoPerfil();

        trocarSenha(dados.senha(), SENHA_NOVA, "Gakki@2028");

        assertThat(textoDe(PaginaPerfil.ERRO_SENHA)).containsIgnoringCase("confirmação");
    }

    @Test
    @DisplayName("RNF0031 — recusa nova senha sem maiúscula e sem caractere especial")
    void recusaNovaSenhaFraca() {
        FluxoDeCadastro.Dados dados = clienteNovoNoPerfil();
        String fraca = GeradorDeDados.senhaFraca();

        trocarSenha(dados.senha(), fraca, fraca);

        assertThat(textoDe(PaginaPerfil.ERRO_SENHA)).containsIgnoringCase("senha");
    }

    private void trocarSenha(String atual, String nova, String confirmacao) {
        preencher(PaginaPerfil.SENHA_ATUAL, atual);
        preencher(PaginaPerfil.SENHA_NOVA, nova);
        preencher(PaginaPerfil.SENHA_CONFIRMACAO, confirmacao);
        clicar(PaginaPerfil.BOTAO_SALVAR_SENHA);
    }
}
