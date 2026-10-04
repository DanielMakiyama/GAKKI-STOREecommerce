-- Log de transação (RNF0012), escopo desta fase: entidades tocadas pelo
-- fluxo de criação de pedido — carrinho, pedido, pagamento, cupom,
-- instrumento/estoque e, por consequência de RF0035/RF0036 tocarem essas
-- entidades dentro deste fluxo, também endereço e cartão (módulo Cliente).
-- Não é retroativo ao restante do CRUD de Cliente (contrato de Vendas,
-- decisão 8).
--
-- Guarda os dados alterados (não só a data/hora/usuário) porque a RN0012
-- do DRS exige "manter os dados alterados" — implica consulta posterior,
-- não só uma linha em arquivo de log de aplicação.

CREATE TABLE log_transacao (
    id               BIGSERIAL     NOT NULL,
    entidade         VARCHAR(60)   NOT NULL,
    entidade_id      VARCHAR(40)   NOT NULL,
    operacao         VARCHAR(12)   NOT NULL,
    usuario_email    VARCHAR(150),
    dados_alterados  JSONB         NOT NULL,
    criado_em        TIMESTAMPTZ   NOT NULL DEFAULT now(),

    CONSTRAINT pk_log_transacao PRIMARY KEY (id),
    CONSTRAINT ck_log_transacao_operacao CHECK (operacao IN ('INSERCAO', 'ALTERACAO'))
);

CREATE INDEX ix_log_transacao_entidade ON log_transacao (entidade, entidade_id);

COMMENT ON TABLE  log_transacao                IS 'Auditoria de escrita do fluxo de vendas (RNF0012): quem, quando e o que mudou';
COMMENT ON COLUMN log_transacao.usuario_email  IS 'Nulo apenas para escrita de sistema sem usuário autenticado no contexto (não deveria ocorrer neste fluxo)';
COMMENT ON COLUMN log_transacao.dados_alterados IS 'Representação JSON dos campos alterados (ALTERACAO) ou do estado inicial (INSERCAO)';