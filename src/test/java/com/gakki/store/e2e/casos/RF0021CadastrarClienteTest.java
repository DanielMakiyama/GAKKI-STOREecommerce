package com.gakki.store.e2e.casos;

import com.gakki.store.e2e.BaseE2ETest;
import com.gakki.store.e2e.GeradorDeDados;
import com.gakki.store.e2e.acoes.FluxoDeCadastro;
import com.gakki.store.e2e.acoes.FluxoDeCadastro.Dados;
import com.gakki.store.e2e.paginas.PaginaRegistrar;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

//RF0021 — Cadastrar cliente.

@DisplayName("RF0021 — Cadastrar cliente")
class RF0021CadastrarClienteTest extends BaseE2ETest {

    @Test
    @DisplayName("Cadastra cliente válido, autentica e entra no catálogo")
    void cadastraClienteValido() {
        submeterCom(Dados.validos());

        // O cadastro já autentica e leva ao catálogo — se chegou lá, o
        // POST devolveu 201 e o login seguinte devolveu um token válido.
        esperarUrlConter("/catalogo");
        assertThat(navegador.getCurrentUrl()).contains("/catalogo");
    }

    @Test
    @DisplayName("RN0026 — recusa cadastro com CPF inválido")
    void recusaCpfInvalido() {
        submeterCom(Dados.validos().comCpf(GeradorDeDados.cpfInvalido()));

        // O @CPF confere os dígitos verificadores, não só o formato:
        // 111.111.111-11 tem 11 dígitos e mesmo assim é recusado.
        assertThat(textoDe(PaginaRegistrar.ERRO)).containsIgnoringCase("CPF inválido");
        assertThat(navegador.getCurrentUrl()).contains("/registrar");
    }

    @Test
    @DisplayName("RNF0031 — recusa senha sem maiúscula e sem caractere especial")
    void recusaSenhaFraca() {
        String fraca = GeradorDeDados.senhaFraca();
        submeterCom(Dados.validos().comSenhas(fraca, fraca));

        assertThat(textoDe(PaginaRegistrar.ERRO)).containsIgnoringCase("senha");
        assertThat(navegador.getCurrentUrl()).contains("/registrar");
    }

    @Test
    @DisplayName("RNF0032 — recusa cadastro com confirmação de senha divergente")
    void recusaConfirmacaoDivergente() {
        submeterCom(Dados.validos().comSenhas("Gakki@2026", "Gakki@2027"));

        assertThat(textoDe(PaginaRegistrar.ERRO)).containsIgnoringCase("confirmação de senha");
        assertThat(navegador.getCurrentUrl()).contains("/registrar");
    }

    @Test
    @DisplayName("Recusa cadastro com e-mail já existente (409)")
    void recusaEmailDuplicado() {
        // O e-mail vem do seed V999 — é o mesmo do cartão de login.
        submeterCom(Dados.validos().comEmail("cliente@gakkistore.com"));

        assertThat(textoDe(PaginaRegistrar.ERRO)).containsIgnoringCase("e-mail");
        assertThat(navegador.getCurrentUrl()).contains("/registrar");
    }

    private void submeterCom(FluxoDeCadastro.Dados dados) {
        cadastro().preencher(dados).submeter();
    }
}
