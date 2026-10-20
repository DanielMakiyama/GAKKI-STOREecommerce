package com.gakki.store.e2e;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.OffsetDateTime;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Atalho pelo banco, para o que a API deliberadamente não oferece.
 *
 * <p>Cupom é de uso único: consumido numa compra, {@code utilizado} vira
 * {@code true} e ele sai de circulação. E não existe endpoint de emissão
 * de cupom — se existisse, qualquer cliente emitiria dinheiro para si
 * mesmo. A consequência é que os casos de RN0035 e RN0036 só rodariam
 * verdes <b>uma vez por banco</b>: na segunda execução os cupons do seed
 * já foram gastos.
 *
 * <p>Inserir por JDBC resolve isso deixando cada caso dono dos seus
 * próprios cupons, com código único. É o que permite rodar a suíte ao
 * vivo na apresentação sem recriar o schema antes.
 *
 * <p>O driver do PostgreSQL já está no classpath de teste (escopo
 * {@code runtime} no pom), e as credenciais são as mesmas do
 * {@code application.yml} — sobrescrevíveis por {@code -De2e.db.*} para
 * quem roda contra outro banco.
 */
public final class BancoDeApoio {

    private static final String URL = System.getProperty("e2e.db.url", "jdbc:postgresql://localhost:5432/gakki");
    private static final String USUARIO = System.getProperty("e2e.db.usuario", "gakki");
    private static final String SENHA = System.getProperty("e2e.db.senha", "gakki");

    /** Cliente de demonstração do seed V999 — dono dos cupons de teste. */
    private static final String CLIENTE_DEMO = "CLI-0001";

    private BancoDeApoio() {
    }

    public static String criarCupomDeTroca(BigDecimal valor) {
        return criarCupom("TROCA", valor);
    }

    public static String criarCupomPromocional(BigDecimal valor) {
        return criarCupom("PROMOCIONAL", valor);
    }

    /**
     * Insere um cupom disponível para o cliente de demonstração e devolve
     * o código gerado.
     *
     * <p>O prefixo {@code E2E-} não colide com os do seed
     * ({@code TROCA-000N}, {@code PROMO-000N}) nem com os emitidos como
     * troco ({@code TROCA-1000} em diante, pela sequence da V14).
     */
    private static String criarCupom(String tipo, BigDecimal valor) {
        String codigo = "E2E-" + System.currentTimeMillis() % 1_000_000L
                + "-" + ThreadLocalRandom.current().nextInt(100, 1000);

        String sql = """
                INSERT INTO cupom (codigo, tipo, cliente_id, valor, utilizado)
                VALUES (?, ?, (SELECT id FROM cliente WHERE codigo = ?), ?, FALSE)""";

        try (Connection conexao = conectar();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setString(1, codigo);
            comando.setString(2, tipo);
            comando.setString(3, CLIENTE_DEMO);
            comando.setBigDecimal(4, valor);
            comando.executeUpdate();

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Falha ao criar cupom de teste em " + URL + ". O banco está no ar e migrado?", e);
        }
        return codigo;
    }

    /** Prova que o cupom saiu de circulação depois da compra (RN0036). */
    public static boolean cupomFoiUtilizado(String codigo) {
        return Boolean.TRUE.equals(umCampoDoCupom(codigo, "utilizado", Boolean.class));
    }

    public static BigDecimal valorDoCupom(String codigo) {
        return umCampoDoCupom(codigo, "valor", BigDecimal.class);
    }

    /**
     * Quantas linhas o log de auditoria tem para uma entidade (RNF0012).
     *
     * <p>É a única forma de verificar o log pela suíte: nenhuma tela
     * exibe {@code log_transacao}.
     */
    public static int linhasDeLog(String entidade) {
        String sql = "SELECT count(*) FROM log_transacao WHERE entidade = ?";
        try (Connection conexao = conectar();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setString(1, entidade);
            try (ResultSet resultado = comando.executeQuery()) {
                resultado.next();
                return resultado.getInt(1);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao consultar log_transacao em " + URL, e);
        }
    }

    /**
     * Uma linha do log de auditoria (RNF0012).
     *
     * <p>A regra exige "data, hora, usuário responsável além de manter os
     * dados alterados" — os quatro campos estão aqui para o teste poder
     * afirmar sobre cada um, em vez de só contar linhas.
     */
    public record Log(String entidade, String entidadeId, String operacao,
                      String usuarioEmail, String dadosAlterados, OffsetDateTime criadoEm) {
    }

    /** Linha mais recente do log para uma entidade, ou {@code null}. */
    public static Log ultimoLog(String entidade) {
        String sql = """
                SELECT entidade, entidade_id, operacao, usuario_email,
                       dados_alterados::text AS dados, criado_em
                  FROM log_transacao
                 WHERE entidade = ?
                 ORDER BY id DESC
                 LIMIT 1""";
        try (Connection conexao = conectar();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setString(1, entidade);
            try (ResultSet r = comando.executeQuery()) {
                if (!r.next()) {
                    return null;
                }
                return new Log(
                        r.getString("entidade"),
                        r.getString("entidade_id"),
                        r.getString("operacao"),
                        r.getString("usuario_email"),
                        r.getString("dados"),
                        r.getObject("criado_em", OffsetDateTime.class));
            }
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao consultar log_transacao em " + URL, e);
        }
    }

    private static <T> T umCampoDoCupom(String codigo, String coluna, Class<T> tipo) {
        // A coluna é escolhida por este arquivo, nunca por entrada
        // externa — não há caminho de injeção aqui.
        String sql = "SELECT " + coluna + " FROM cupom WHERE codigo = ?";
        try (Connection conexao = conectar();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setString(1, codigo);
            try (ResultSet resultado = comando.executeQuery()) {
                if (!resultado.next()) {
                    throw new IllegalStateException("Nenhum cupom com o código " + codigo);
                }
                return resultado.getObject(1, tipo);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao consultar o cupom " + codigo + " em " + URL, e);
        }
    }

    // ------------------------------------------------------------
    // Bloqueio do carrinho (RN0044, RN0045, RNF0042)
    //
    // O prazo é parametrizado por `gakki.carrinho.prazo-bloqueio` e vale
    // 15 minutos na demonstração. Esperar o relógio passar não é teste:
    // é sorteio. Empurrar `bloqueado_ate` para o passado produz o mesmo
    // estado que o tempo produziria, em milissegundos, e a expiração em
    // si continua sendo decidida pelo backend no próximo acesso — é o
    // caminho de produção que roda, não um atalho que simula o efeito.
    // ------------------------------------------------------------

    /** Põe o prazo do carrinho no passado. Devolve quantas linhas mudaram. */
    public static int vencerPrazoDoCarrinho() {
        String sql = """
                UPDATE carrinho
                   SET bloqueado_ate = now() - interval '1 second'
                 WHERE cliente_id = (SELECT id FROM cliente WHERE codigo = ?)""";

        try (Connection conexao = conectar();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setString(1, CLIENTE_DEMO);
            return comando.executeUpdate();

        } catch (Exception e) {
            throw new IllegalStateException("Falha ao vencer o prazo do carrinho em " + URL, e);
        }
    }

    /** Fim do prazo gravado, ou {@code null} se não há contagem em curso. */
    public static OffsetDateTime prazoDoCarrinho() {
        String sql = """
                SELECT c.bloqueado_ate
                  FROM carrinho c
                  JOIN cliente cl ON cl.id = c.cliente_id
                 WHERE cl.codigo = ?""";

        try (Connection conexao = conectar();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setString(1, CLIENTE_DEMO);
            try (ResultSet r = comando.executeQuery()) {
                return r.next() ? r.getObject(1, OffsetDateTime.class) : null;
            }
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao consultar o prazo do carrinho em " + URL, e);
        }
    }

    /** Quantas linhas de `item_retirado` o carrinho do cliente tem (RNF0042). */
    public static int itensRetirados() {
        String sql = """
                SELECT count(*)
                  FROM item_retirado r
                  JOIN carrinho c ON c.id = r.carrinho_id
                  JOIN cliente cl ON cl.id = c.cliente_id
                 WHERE cl.codigo = ?""";

        try (Connection conexao = conectar();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setString(1, CLIENTE_DEMO);
            try (ResultSet r = comando.executeQuery()) {
                r.next();
                return r.getInt(1);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao contar itens retirados em " + URL, e);
        }
    }

    /**
     * Devolve unidades ao estoque do instrumento.
     *
     * <p>Existe para a suíte poder rodar mais de uma vez: desde que a
     * RN0028 entrou, a aprovação do pagamento debita estoque de verdade,
     * e um caso que compra 2 pedais a cada execução esgotaria os 8 do
     * seed na quinta rodada.
     *
     * <p>Não passa pela entrada de estoque da API de propósito: aquela
     * recalcula o valor de venda pelo maior custo (RN0051) e mexeria nos
     * preços que as outras asserções conferem. Isto aqui é desfazer o
     * efeito do caso, não exercitar um fluxo.
     */
    public static void reporEstoque(String codigo, int quantidade) {
        String sql = """
                UPDATE instrumento
                   SET quantidade_estoque = quantidade_estoque + ?
                 WHERE codigo = ?""";

        try (Connection conexao = conectar();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setInt(1, quantidade);
            comando.setString(2, codigo);
            if (comando.executeUpdate() != 1) {
                throw new IllegalStateException("Nenhum instrumento com o código " + codigo);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao repor o estoque de " + codigo + " em " + URL, e);
        }
    }

    // ------------------------------------------------------------
    // Troca (RF0041 a RF0045)
    // ------------------------------------------------------------

    /**
     * Status gravado da troca, lido direto do banco.
     *
     * <p>Serve para provar que a transição sobreviveu ao commit, e não só
     * que a tela mostrou o rótulo novo.
     */
    public static String statusDaTroca(String numero) {
        return umCampoDaTroca(numero, "status", String.class);
    }

    /** Decisão sobre o estoque na confirmação de recebimento (RF0044). */
    public static Boolean trocaRetornouAoEstoque(String numero) {
        return umCampoDaTroca(numero, "retorna_estoque", Boolean.class);
    }

    private static <T> T umCampoDaTroca(String numero, String coluna, Class<T> tipo) {
        // Coluna escolhida por este arquivo, nunca por entrada externa.
        String sql = "SELECT " + coluna + " FROM troca WHERE numero = ?";
        try (Connection conexao = conectar();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setString(1, numero);
            try (ResultSet r = comando.executeQuery()) {
                if (!r.next()) {
                    throw new IllegalStateException("Nenhuma troca com o número " + numero);
                }
                return r.getObject(1, tipo);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao consultar a troca " + numero + " em " + URL, e);
        }
    }

    private static Connection conectar() throws Exception {
        return DriverManager.getConnection(URL, USUARIO, SENHA);
    }
}
