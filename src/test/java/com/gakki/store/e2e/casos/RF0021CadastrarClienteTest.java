package com.gakki.store.e2e.casos;

import com.gakki.store.e2e.BaseE2ETest;
import com.gakki.store.e2e.GeradorDeDados;
import com.gakki.store.e2e.paginas.PaginaRegistrar;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.support.ui.Select;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RF0021 — Cadastrar cliente.
 *
 * <p>Cada teste nomeia a regra que valida. A saída do
 * {@code mvn test -Pe2e} vira a matriz de rastreabilidade da
 * apresentação, sem precisar montar tabela à mão.
 */
@DisplayName("RF0021 — Cadastrar cliente")
class RF0021CadastrarClienteTest extends BaseE2ETest {

    @Test
    @DisplayName("Cadastra cliente válido, autentica e entra no catálogo")
    void cadastraClienteValido() {
        preencherCadastroValido(GeradorDeDados.emailUnico(), GeradorDeDados.cpfValido(),
                GeradorDeDados.senhaForte(), GeradorDeDados.senhaForte());

        clicar(PaginaRegistrar.BOTAO_CRIAR);

        // O cadastro já autentica e leva ao catálogo — se chegou lá, o
        // POST devolveu 201 e o login seguinte devolveu um token válido.
        esperarUrlConter("/catalogo");
        assertThat(navegador.getCurrentUrl()).contains("/catalogo");
    }

    @Test
    @DisplayName("RN0026 — recusa cadastro com CPF inválido")
    void recusaCpfInvalido() {
        preencherCadastroValido(GeradorDeDados.emailUnico(), GeradorDeDados.cpfInvalido(),
                GeradorDeDados.senhaForte(), GeradorDeDados.senhaForte());

        clicar(PaginaRegistrar.BOTAO_CRIAR);

        // O @CPF confere os dígitos verificadores, não só o formato:
        // 111.111.111-11 tem 11 dígitos e mesmo assim é recusado.
        assertThat(textoDe(PaginaRegistrar.ERRO)).containsIgnoringCase("CPF inválido");
        assertThat(navegador.getCurrentUrl()).contains("/registrar");
    }

    @Test
    @DisplayName("RNF0031 — recusa senha sem maiúscula e sem caractere especial")
    void recusaSenhaFraca() {
        preencherCadastroValido(GeradorDeDados.emailUnico(), GeradorDeDados.cpfValido(),
                GeradorDeDados.senhaFraca(), GeradorDeDados.senhaFraca());

        clicar(PaginaRegistrar.BOTAO_CRIAR);

        assertThat(textoDe(PaginaRegistrar.ERRO)).containsIgnoringCase("senha");
        assertThat(navegador.getCurrentUrl()).contains("/registrar");
    }

    @Test
    @DisplayName("RNF0032 — recusa cadastro com confirmação de senha divergente")
    void recusaConfirmacaoDivergente() {
        preencherCadastroValido(GeradorDeDados.emailUnico(), GeradorDeDados.cpfValido(),
                "Gakki@2026", "Gakki@2027");

        clicar(PaginaRegistrar.BOTAO_CRIAR);

        assertThat(textoDe(PaginaRegistrar.ERRO)).containsIgnoringCase("confirmação de senha");
        assertThat(navegador.getCurrentUrl()).contains("/registrar");
    }

    @Test
    @DisplayName("Recusa cadastro com e-mail já existente (409)")
    void recusaEmailDuplicado() {
        // O e-mail vem do seed V999 — é o mesmo do cartão de login.
        preencherCadastroValido("cliente@gakkistore.com", GeradorDeDados.cpfValido(),
                GeradorDeDados.senhaForte(), GeradorDeDados.senhaForte());

        clicar(PaginaRegistrar.BOTAO_CRIAR);

        assertThat(textoDe(PaginaRegistrar.ERRO)).containsIgnoringCase("e-mail");
        assertThat(navegador.getCurrentUrl()).contains("/registrar");
    }

    /**
     * Preenche o formulário inteiro com dados válidos, variando só o que
     * cada teste quer exercitar.
     *
     * <p>A alternativa seria repetir vinte {@code preencher()} em cada
     * teste — e aí um campo novo no formulário quebraria os cinco de uma
     * vez, em vez de um método só.
     */
    private void preencherCadastroValido(String email, String cpf, String senha, String confirmacao) {
        abrir(PaginaRegistrar.CAMINHO);

        preencher(PaginaRegistrar.NOME, "Cliente de Teste E2E");
        preencher(PaginaRegistrar.EMAIL, email);
        preencher(PaginaRegistrar.CPF, cpf);
        selecionarPorValor(PaginaRegistrar.GENERO, "MASCULINO");
        preencher(PaginaRegistrar.NASCIMENTO, "14/05/2000");
        selecionarPorValor(PaginaRegistrar.TELEFONE_TIPO, "CELULAR");
        preencher(PaginaRegistrar.TELEFONE_DDD, "11");
        preencher(PaginaRegistrar.TELEFONE_NUMERO, "988887777");
        preencher(PaginaRegistrar.SENHA, senha);
        preencher(PaginaRegistrar.CONFIRMACAO, confirmacao);

        preencher(PaginaRegistrar.APELIDO, "Casa");
        selecionarPorValor(PaginaRegistrar.TIPO_RESIDENCIA, "APARTAMENTO");
        selecionarPorValor(PaginaRegistrar.TIPO_LOGRADOURO, "RUA");
        preencher(PaginaRegistrar.LOGRADOURO, "das Guitarras");
        preencher(PaginaRegistrar.NUMERO, "123");
        preencher(PaginaRegistrar.BAIRRO, "Centro");
        preencher(PaginaRegistrar.CIDADE, "Sao Paulo");
        preencher(PaginaRegistrar.UF, "SP");
        preencher(PaginaRegistrar.CEP, "01000000");
        preencher(PaginaRegistrar.PAIS, "Brasil");
    }

    private void selecionarPorValor(org.openqa.selenium.By seletor, String valor) {
        new Select(esperarPor(seletor)).selectByValue(valor);
    }
}
