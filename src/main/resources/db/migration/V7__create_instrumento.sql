-- Catálogo de instrumentos (RF0011).
--
-- `quantidade_estoque` é somente LIDA pelo fluxo de criação de pedido. A
-- RN0031 nesta fatia é um SELECT seguido de comparação: adicionar ao
-- carrinho e finalizar a compra não decrementam nada (contrato de Vendas
-- v3, decisão 1). A baixa definitiva (RF0053/RN0028) e a reserva com
-- bloqueio (RN0044) estão fora do escopo desta fase por exclusão explícita
-- do enunciado — a coluna já nasce pronta para quando entrarem.

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
COMMENT ON COLUMN instrumento.quantidade_estoque  IS 'Estoque disponível; nesta fase apenas lido para validar a RN0031 — nenhuma escrita decrementa a coluna (RF0053/RN0028 fora de escopo)';
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