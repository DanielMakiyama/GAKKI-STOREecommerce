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

    private static Connection conectar() throws Exception {
        return DriverManager.getConnection(URL, USUARIO, SENHA);
    }
}
