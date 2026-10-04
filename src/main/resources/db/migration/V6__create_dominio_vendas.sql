-- Tabelas de domínio do módulo de Vendas.
--
-- Mesmo raciocínio de `bandeira` (V3): são cadastros que o instrumento e a
-- entrada em estoque referenciam por FK, em vez de strings soltas no
-- código ou no front.

CREATE TABLE categoria (
                           id   BIGSERIAL   NOT NULL,
                           nome VARCHAR(80) NOT NULL,

                           CONSTRAINT pk_categoria      PRIMARY KEY (id),
                           CONSTRAINT uk_categoria_nome UNIQUE (nome)
);

COMMENT ON TABLE categoria IS 'Categorias de instrumento (RN0012 — um instrumento pode ter mais de uma)';

CREATE TABLE fabricante (
                            id   BIGSERIAL   NOT NULL,
                            nome VARCHAR(80) NOT NULL,

                            CONSTRAINT pk_fabricante      PRIMARY KEY (id),
                            CONSTRAINT uk_fabricante_nome UNIQUE (nome)
);

COMMENT ON TABLE fabricante IS 'Fabricantes/marcas dos instrumentos cadastrados';

-- Diferente de `bandeira`, a RN0050 exige o fornecedor em toda entrada em
-- estoque, mas nenhum RF/RN detalha cadastro ou validação própria de
-- fornecedor — por isso não há coluna `ativo` nem regra de unicidade além
-- do nome. É criado por "find-or-create" a partir do texto livre que o
-- formulário de estoque já envia (contrato de Vendas, decisão 4).
CREATE TABLE fornecedor (
                            id   BIGSERIAL    NOT NULL,
                            nome VARCHAR(120) NOT NULL,

                            CONSTRAINT pk_fornecedor      PRIMARY KEY (id),
                            CONSTRAINT uk_fornecedor_nome UNIQUE (nome)
);

COMMENT ON TABLE fornecedor IS 'Fornecedores de lotes de estoque (RN0050), criado por find-or-create';

-- RN0013/RF0052: o valor de venda do instrumento deriva do custo mais a
-- margem do grupo ao qual pertence.
CREATE TABLE grupo_precificacao (
                                    id                BIGSERIAL   NOT NULL,
                                    nome              VARCHAR(60) NOT NULL,
                                    margem_percentual NUMERIC(5, 2) NOT NULL,
                                    ativo             BOOLEAN     NOT NULL DEFAULT TRUE,

                                    CONSTRAINT pk_grupo_precificacao      PRIMARY KEY (id),
                                    CONSTRAINT uk_grupo_precificacao_nome UNIQUE (nome),
                                    CONSTRAINT ck_grupo_precificacao_margem CHECK (margem_percentual >= 0)
);

COMMENT ON TABLE  grupo_precificacao                    IS 'Grupos de margem de lucro aplicados ao cálculo do valor de venda (RN0013, RF0052)';
COMMENT ON COLUMN grupo_precificacao.margem_percentual  IS 'Percentual aplicado sobre o maior custo registrado do instrumento (RN0051), ex.: 40.00 para 40%';