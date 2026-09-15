package com.gakki.store.repository.spec;

import com.gakki.store.domain.Cliente;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Filtros da consulta administrativa de clientes (RF0024).
 *
 * <p>A RF0024 permite qualquer campo como filtro, isolado ou combinado.
 * Specification resolve isso montando o WHERE só com o que veio
 * preenchido — diferente de um JPQL com {@code (:nome is null or ...)},
 * que manda todos os parâmetros em toda consulta e deixa o plano de
 * execução do banco pior a cada campo novo.
 */
public final class ClienteSpecification {

    private ClienteSpecification() {
    }

    public static Specification<Cliente> comFiltros(String nome,
                                                    String email,
                                                    String cpf,
                                                    String codigo,
                                                    Boolean ativo) {
        List<Specification<Cliente>> filtros = new ArrayList<>();

        if (preenchido(nome)) {
            filtros.add(nomeContendo(nome));
        }
        if (preenchido(email)) {
            filtros.add(emailContendo(email));
        }
        if (preenchido(cpf)) {
            filtros.add(cpfIgual(cpf));
        }
        if (preenchido(codigo)) {
            filtros.add(codigoIgual(codigo));
        }
        if (ativo != null) {
            filtros.add(ativoIgual(ativo));
        }

        return Specification.allOf(filtros);
    }

    public static Specification<Cliente> nomeContendo(String nome) {
        // lower() dos dois lados casa com o índice ix_cliente_nome,
        // criado em lower(nome) justamente para esta consulta.
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("nome")), "%" + nome.toLowerCase() + "%");
    }

    public static Specification<Cliente> emailContendo(String email) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("usuario").get("email")), "%" + email.toLowerCase() + "%");
    }

    public static Specification<Cliente> cpfIgual(String cpf) {
        return (root, query, cb) -> cb.equal(root.get("cpf"), somenteDigitos(cpf));
    }

    public static Specification<Cliente> codigoIgual(String codigo) {
        return (root, query, cb) -> cb.equal(cb.upper(root.get("codigo")), codigo.toUpperCase());
    }

    public static Specification<Cliente> ativoIgual(boolean ativo) {
        return (root, query, cb) -> cb.equal(root.get("usuario").get("ativo"), ativo);
    }

    private static boolean preenchido(String valor) {
        return valor != null && !valor.isBlank();
    }

    private static String somenteDigitos(String valor) {
        return valor.replaceAll("\\D", "");
    }
}
