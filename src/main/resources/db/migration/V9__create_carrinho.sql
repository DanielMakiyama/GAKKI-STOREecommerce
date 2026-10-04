-- Carrinho de compra (RF0031, RF0032, RN0044).
--
-- 1:1 com `cliente`, criado sob demanda na primeira chamada de
-- "adicionar ao carrinho" — não existe carrinho anônimo nesta fatia, nem
-- linha de carrinho vazio no seed (contrato de Vendas, decisão 9).
--
-- `bloqueado_ate` é um único timer por carrinho, reiniciado a cada item
-- adicionado, e não um timer por item. O texto da RN0044/RN0045 permite
-- duas leituras quanto ao escopo do bloqueio; a leitura "carrinho inteiro"
-- foi a adotada, registrada como decisão 6 do contrato e como observação
-- na especificação de caso de uso — não é algo que o DRS resolve sozinho.

CREATE TABLE carrinho (
                          id            BIGSERIAL   NOT NULL,
                          cliente_id    BIGINT      NOT NULL,
                          bloqueado_ate TIMESTAMPTZ,

                          CONSTRAINT pk_carrinho         PRIMARY KEY (id),
                          CONSTRAINT fk_carrinho_cliente FOREIGN KEY (cliente_id) REFERENCES cliente (id),
                          CONSTRAINT uk_carrinho_cliente UNIQUE (cliente_id)
);

COMMENT ON TABLE  carrinho               IS 'Carrinho de compra do cliente, um por cliente (RF0031)';
COMMENT ON COLUMN carrinho.bloqueado_ate IS 'Instante em que o bloqueio dos itens reservados expira (RN0044); nulo quando o carrinho está vazio';

CREATE TABLE item_carrinho (
                               id              BIGSERIAL     NOT NULL,
                               carrinho_id     BIGINT        NOT NULL,
                               instrumento_id  BIGINT        NOT NULL,
                               quantidade      INTEGER       NOT NULL,
                               valor_unitario  NUMERIC(12,2) NOT NULL,

                               CONSTRAINT pk_item_carrinho               PRIMARY KEY (id),
                               CONSTRAINT fk_item_carrinho_carrinho      FOREIGN KEY (carrinho_id)    REFERENCES carrinho (id),
                               CONSTRAINT fk_item_carrinho_instrumento   FOREIGN KEY (instrumento_id) REFERENCES instrumento (id),

                               CONSTRAINT ck_item_carrinho_quantidade CHECK (quantidade > 0)
);

-- Um instrumento ocupa uma única linha por carrinho; adicionar de novo soma
-- na quantidade existente, em vez de criar uma segunda linha (mesmo
-- comportamento já visível no protótipo mockado).
CREATE UNIQUE INDEX ux_item_carrinho_instrumento ON item_carrinho (carrinho_id, instrumento_id);

COMMENT ON TABLE  item_carrinho                IS 'Itens reservados no carrinho (RF0031, RF0032)';
COMMENT ON COLUMN item_carrinho.valor_unitario IS 'Fotografia do valor de venda do instrumento no momento em que foi adicionado ao carrinho';