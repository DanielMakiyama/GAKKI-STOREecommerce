-- Catálogo de instrumentos (RF0011).
--
-- `quantidade_estoque` acumula uma função dupla, por decisão registrada no
-- contrato de Vendas (decisão 2): é tanto o estoque disponível quanto a
-- reserva do carrinho. Adicionar ao carrinho decrementa esta coluna na
-- hora — é o que impede dois clientes de reservarem o mesmo item (RN0031,
-- RN0044); a baixa definitiva da RF0053 não precisa descontar de novo,
-- porque o desconto já aconteceu na reserva.

-- Sequence dedicada ao código do instrumento, mesmo padrão de
-- `seq_codigo_cliente` (V2): o service consome o nextval e formata como
-- INST-0001.
CREATE SEQUENCE seq_codigo_instrumento START WITH 1 INCREMENT BY 1;

CREATE TABLE instrumento (
                             id                     BIGSERIAL     NOT NULL,
                             codigo                 VARCHAR(20)   NOT NULL,
                             nome                   VARCHAR(150)  NOT NULL,
                             descricao              VARCHAR(500)  NOT NULL,
                             fabricante_id          BIGINT        NOT NULL,
                             grupo_precificacao_id  BIGINT        NOT NULL,
                             ano_fabricacao         SMALLINT      NOT NULL,
                             valor_venda            NUMERIC(12,2) NOT NULL DEFAULT 0,
                             quantidade_estoque     INTEGER       NOT NULL DEFAULT 0,
                             ativo                  BOOLEAN       NOT NULL DEFAULT TRUE,
                             criado_em              TIMESTAMPTZ   NOT NULL DEFAULT now(),

                             CONSTRAINT pk_instrumento                PRIMARY KEY (id),
                             CONSTRAINT fk_instrumento_fabricante     FOREIGN KEY (fabricante_id)         REFERENCES fabricante (id),
                             CONSTRAINT fk_instrumento_grupo_preco    FOREIGN KEY (grupo_precificacao_id) REFERENCES grupo_precificacao (id),
                             CONSTRAINT uk_instrumento_codigo         UNIQUE (codigo),

                             CONSTRAINT ck_instrumento_valor_venda_nao_negativo  CHECK (valor_venda >= 0),
                             CONSTRAINT ck_instrumento_estoque_nao_negativo      CHECK (quantidade_estoque >= 0)
);

-- Índice funcional para busca por nome no catálogo, mesmo raciocínio de
-- `ix_cliente_nome` (V2): case-insensitive, sem varrer a tabela inteira.
CREATE INDEX ix_instrumento_nome ON instrumento (lower(nome));

COMMENT ON TABLE  instrumento                     IS 'Catálogo de instrumentos musicais (RF0011)';
COMMENT ON COLUMN instrumento.codigo              IS 'Código único no formato INST-0001';
COMMENT ON COLUMN instrumento.valor_venda         IS 'Recalculado a cada entrada em estoque, sobre o maior custo já registrado (RN0051)';
COMMENT ON COLUMN instrumento.quantidade_estoque  IS 'Estoque disponível; decrementado na reserva do carrinho, não apenas na venda confirmada (RN0031, RN0044)';
COMMENT ON COLUMN instrumento.ativo               IS 'FALSE = inativado (RF0012); fora do escopo desta fatia, mas a coluna já nasce pronta';

-- Junção N:N (RN0012 — um instrumento pode estar em mais de uma categoria).
-- Sem entidade associativa própria: o vínculo não carrega nenhum atributo
-- além das duas chaves.
CREATE TABLE instrumento_categoria (
                                       instrumento_id BIGINT NOT NULL,
                                       categoria_id   BIGINT NOT NULL,

                                       CONSTRAINT pk_instrumento_categoria            PRIMARY KEY (instrumento_id, categoria_id),
                                       CONSTRAINT fk_instrumento_categoria_instrumento FOREIGN KEY (instrumento_id) REFERENCES instrumento (id),
                                       CONSTRAINT fk_instrumento_categoria_categoria   FOREIGN KEY (categoria_id)   REFERENCES categoria (id)
);

COMMENT ON TABLE instrumento_categoria IS 'Vínculo N:N entre instrumento e categoria (RN0012)';