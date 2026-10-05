-- Lotes de entrada em estoque (RF0051, RN0050).
--
-- Cada linha é uma entrada física, com seu próprio custo, fornecedor e
-- data — preservada para auditoria, ainda que a saída (RF0053) não
-- rastreie lote a lote: a baixa em estoque está fora do escopo desta
-- fatia, então nada consome lote nesta fase. É o
-- histórico de custos que alimenta o recálculo da RN0051: o valor de venda
-- do instrumento é sempre baseado no MAIOR custo já registrado entre
-- todos os lotes.

CREATE TABLE item_estoque (
                              id             BIGSERIAL     NOT NULL,
                              instrumento_id BIGINT        NOT NULL,
                              fornecedor_id  BIGINT        NOT NULL,
                              quantidade     INTEGER       NOT NULL,
                              valor_custo    NUMERIC(12,2) NOT NULL,
                              data_entrada   DATE          NOT NULL,
                              criado_em      TIMESTAMPTZ   NOT NULL DEFAULT now(),

                              CONSTRAINT pk_item_estoque                PRIMARY KEY (id),
                              CONSTRAINT fk_item_estoque_instrumento    FOREIGN KEY (instrumento_id) REFERENCES instrumento (id),
                              CONSTRAINT fk_item_estoque_fornecedor     FOREIGN KEY (fornecedor_id)  REFERENCES fornecedor (id),

    -- RN0061: não é permitida entrada com quantidade igual a zero (nem negativa).
                              CONSTRAINT ck_item_estoque_quantidade CHECK (quantidade > 0),
    -- RN0062: todo item precisa ter um valor de custo.
                              CONSTRAINT ck_item_estoque_custo      CHECK (valor_custo > 0)
);

CREATE INDEX ix_item_estoque_instrumento ON item_estoque (instrumento_id);

COMMENT ON TABLE  item_estoque              IS 'Histórico de lotes de entrada em estoque, por fornecedor e custo (RF0051, RN0050)';
COMMENT ON COLUMN item_estoque.data_entrada IS 'Obrigatória — nenhum item é registrado sem data de entrada (RNF0064)';
COMMENT ON COLUMN item_estoque.valor_custo  IS 'Custo unitário deste lote; o valor de venda do instrumento usa o maior custo entre todos os lotes (RN0051)';