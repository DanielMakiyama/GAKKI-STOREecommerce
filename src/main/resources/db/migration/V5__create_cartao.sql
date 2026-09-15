-- Cartões de crédito do cliente (RN0024, RF0027).
--
-- Divergência consciente da RN0024: o número completo e o código de
-- segurança NÃO são armazenados. Guardá-los exige certificação PCI-DSS, e
-- num fluxo real esses dados vão direto ao gateway, que devolve um token.
-- Ficam apenas os quatro últimos dígitos, suficientes para o cliente
-- reconhecer o cartão na tela de pagamento.

CREATE TABLE cartao (
    id              BIGSERIAL    NOT NULL,
    cliente_id      BIGINT       NOT NULL,
    bandeira_id     BIGINT       NOT NULL,
    apelido         VARCHAR(40)  NOT NULL,
    ultimos_digitos VARCHAR(4)   NOT NULL,
    nome_titular    VARCHAR(100) NOT NULL,
    validade_mes    SMALLINT     NOT NULL,
    validade_ano    SMALLINT     NOT NULL,
    preferencial    BOOLEAN      NOT NULL DEFAULT FALSE,

    CONSTRAINT pk_cartao          PRIMARY KEY (id),
    CONSTRAINT fk_cartao_cliente  FOREIGN KEY (cliente_id)  REFERENCES cliente (id),
    CONSTRAINT fk_cartao_bandeira FOREIGN KEY (bandeira_id) REFERENCES bandeira (id),

    CONSTRAINT ck_cartao_digitos CHECK (ultimos_digitos ~ '^[0-9]{4}$'),
    CONSTRAINT ck_cartao_mes     CHECK (validade_mes BETWEEN 1 AND 12),
    CONSTRAINT ck_cartao_ano     CHECK (validade_ano BETWEEN 2000 AND 2100)
);

CREATE INDEX ix_cartao_cliente ON cartao (cliente_id);

-- Um único cartão preferencial por cliente (RF0027).
CREATE UNIQUE INDEX ux_cartao_preferencial ON cartao (cliente_id) WHERE preferencial;

COMMENT ON TABLE  cartao                 IS 'Cartões de crédito do cliente (RN0024, RN0025, RF0027)';
COMMENT ON COLUMN cartao.ultimos_digitos IS 'Apenas os 4 últimos dígitos — número completo e CVV nunca são armazenados';
COMMENT ON COLUMN cartao.preferencial    IS 'Cartão sugerido por padrão no checkout (RF0027)';
