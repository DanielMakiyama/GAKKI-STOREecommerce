-- Cupons de troca e promocionais (RF0037, RN0033, RN0036).
--
-- O processo de GERAÇÃO de cupom de troca pelo fluxo de devolução de
-- produto (RF0045) está fora de escopo desta fase; a geração pelo TROCO
-- de um pagamento em cupons (RN0036) está dentro. É o mesmo tipo de dado
-- produzido por dois caminhos diferentes — só um deles existe ainda.
--
-- Nesta fase, os cupons usados na demonstração são carregados por seed
-- (V999), conforme o próprio enunciado da atividade autoriza; não há
-- endpoint de cadastro de cupom promocional.

CREATE TABLE cupom (
    id                     BIGSERIAL     NOT NULL,
    codigo                 VARCHAR(20)   NOT NULL,
    tipo                   VARCHAR(20)   NOT NULL,
    cliente_id             BIGINT        NOT NULL,
    valor                  NUMERIC(12,2) NOT NULL,
    utilizado              BOOLEAN       NOT NULL DEFAULT FALSE,
    pedido_utilizacao_id   BIGINT,
    criado_em              TIMESTAMPTZ   NOT NULL DEFAULT now(),

    CONSTRAINT pk_cupom           PRIMARY KEY (id),
    CONSTRAINT fk_cupom_cliente   FOREIGN KEY (cliente_id) REFERENCES cliente (id),
    CONSTRAINT uk_cupom_codigo    UNIQUE (codigo),

    CONSTRAINT ck_cupom_tipo  CHECK (tipo IN ('TROCA', 'PROMOCIONAL')),
    CONSTRAINT ck_cupom_valor CHECK (valor > 0)
);

-- fk_cupom_pedido_utilizacao vem depois de `pedido` (V10) já existir, o que
-- já é o caso nesta ordem — referenciada aqui sem problema.
ALTER TABLE cupom
    ADD CONSTRAINT fk_cupom_pedido_utilizacao FOREIGN KEY (pedido_utilizacao_id) REFERENCES pedido (id);

CREATE INDEX ix_cupom_cliente ON cupom (cliente_id);

COMMENT ON TABLE  cupom                      IS 'Cupons de troca e promocionais utilizáveis como forma de pagamento (RF0037)';
COMMENT ON COLUMN cupom.tipo                 IS 'TROCA ou PROMOCIONAL — RN0033 limita a 1 promocional por compra, sem limite documentado para cupons de troca';
COMMENT ON COLUMN cupom.utilizado            IS 'TRUE depois de consumido num pagamento; cupom usado não pode ser reaplicado';
COMMENT ON COLUMN cupom.pedido_utilizacao_id IS 'Pedido em que o cupom foi consumido; nulo enquanto não utilizado';