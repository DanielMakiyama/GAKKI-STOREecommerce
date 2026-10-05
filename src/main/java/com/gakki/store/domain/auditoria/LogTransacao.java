package com.gakki.store.domain.auditoria;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;

// Log de transação (RNF0012): para toda escrita, data, hora, usuário
// responsável e os dados alterados.
//
// Fica em domain.auditoria, e não em domain.vendas, porque o log não é
// conceito de venda: ele cobre também endereço e cartão quando o fluxo de
// compra os toca (contrato de Vendas v3, decisão 8).
//
// Guardar os dados alterados numa tabela, e não só numa linha de arquivo
// de log, é o que o "manter os dados alterados" do RNF0012 exige —
// implica poder consultar depois.

@Entity
@Table(name = "log_transacao")
@Getter
@Setter
@NoArgsConstructor
public class LogTransacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nome simples da classe, ex.: "Pedido".
    @Column(nullable = false, length = 60)
    private String entidade;

    // VARCHAR e não BIGINT: serve para qualquer entidade, inclusive as de
    // chave composta, sem uma coluna por tipo de id.
    @Column(name = "entidade_id", nullable = false, length = 40)
    private String entidadeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private OperacaoAuditoria operacao;

    // Nulo apenas em escrita de sistema sem usuário no SecurityContext —
    // não deveria ocorrer neste fluxo.
    @Column(name = "usuario_email", length = 150)
    private String usuarioEmail;

    // JSONB no Postgres. O campo é String em Java: quem serializa é o
    // registrador de auditoria, com Jackson, antes de gravar.
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "dados_alterados", nullable = false)
    private String dadosAlterados;

    @CreationTimestamp
    @Column(name = "criado_em", nullable = false, updatable = false)
    private OffsetDateTime criadoEm;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LogTransacao outro)) {
            return false;
        }
        return id != null && id.equals(outro.getId());
    }

    @Override
    public int hashCode() {
        return LogTransacao.class.hashCode();
    }
}
