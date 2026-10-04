-- Linhas de pagamento de um pedido (RF0036, RF0037).
--
-- Uma linha é cartão OU cupom, nunca os dois — RN0034 (mínimo de R$10 por
-- cartão) não é incondicional: RN0035 permite uma linha de cartão abaixo
-- de R$10 quando há cupom no mesmo pedido. Essa condicional depende de
-- outras linhas do mesmo pedido, então não dá para expressar em CHECK de
-- tabela sem trigger — fica a cargo do PedidoService (contrato de Vendas,
-- decisão 6).

CREATE TABLE pagamento (
    id         BIGSERIAL     NOT NULL,
    pedido_id  BIGINT        NOT NULL,
    cartao_id  BIGINT,
    cupom_id   BIGINT,
    valor      NUMERIC(12,2) NOT NULL,

    CONSTRAINT pk_pagamento        PRIMARY KEY (id),
    CONSTRAINT fk_pagamento_pedido FOREIGN KEY (pedido_id) REFERENCES pedido (id),
    CONSTRAINT fk_pagamento_cartao FOREIGN KEY (cartao_id) REFERENCES cartao (id),
    CONSTRAINT fk_pagamento_cupom  FOREIGN KEY (cupom_id)  REFERENCES cupom (id),

    CONSTRAINT ck_pagamento_valor_positivo CHECK (valor > 0),
    -- Exatamente um dos dois: nunca os dois nulos, nunca os dois preenchidos.
    CONSTRAINT ck_pagamento_um_tipo CHECK (
        (cartao_id IS NOT NULL AND cupom_id IS NULL) OR
        (cartao_id IS NULL AND cupom_id IS NOT NULL)
    )
);

CREATE INDEX ix_pagamento_pedido ON pagamento (pedido_id);

COMMENT ON TABLE  pagamento       IS 'Linhas de pagamento de um pedido — cartão (RF0036) ou cupom (RF0037), nunca os dois na mesma linha';
COMMENT ON COLUMN pagamento.valor IS 'Piso de R$10 por linha de cartão é regra condicional (RN0034/RN0035), validada no service, não aqui';