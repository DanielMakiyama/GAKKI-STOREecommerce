-- Dados cadastrais do cliente (RN0026).
--
-- Relação 1:1 com `usuario`, garantida pela UNIQUE em usuario_id — sem ela
-- a anotação @OneToOne do JPA seria só uma promessa, e o banco aceitaria
-- dois clientes para a mesma credencial.

-- Sequence dedicada ao código de cliente (RNF0035). O service consome o
-- nextval e formata como CLI-0001.
CREATE SEQUENCE seq_codigo_cliente START WITH 1 INCREMENT BY 1;

CREATE TABLE cliente (
    id              BIGSERIAL    NOT NULL,
    usuario_id      BIGINT       NOT NULL,
    codigo          VARCHAR(20)  NOT NULL,
    nome            VARCHAR(150) NOT NULL,
    cpf             VARCHAR(11)  NOT NULL,
    genero          VARCHAR(20)  NOT NULL,
    data_nascimento DATE         NOT NULL,
    telefone_tipo   VARCHAR(12)  NOT NULL,
    telefone_ddd    VARCHAR(2)   NOT NULL,
    telefone_numero VARCHAR(9)   NOT NULL,
    criado_em       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    atualizado_em   TIMESTAMPTZ,

    CONSTRAINT pk_cliente         PRIMARY KEY (id),
    CONSTRAINT fk_cliente_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id),
    CONSTRAINT uk_cliente_usuario UNIQUE (usuario_id),
    CONSTRAINT uk_cliente_codigo  UNIQUE (codigo),
    CONSTRAINT uk_cliente_cpf     UNIQUE (cpf),

    CONSTRAINT ck_cliente_genero         CHECK (genero IN ('MASCULINO', 'FEMININO', 'OUTRO', 'NAO_INFORMADO')),
    CONSTRAINT ck_cliente_telefone_tipo  CHECK (telefone_tipo IN ('CELULAR', 'RESIDENCIAL', 'COMERCIAL')),
    CONSTRAINT ck_cliente_cpf_digitos    CHECK (cpf ~ '^[0-9]{11}$'),
    CONSTRAINT ck_cliente_ddd_digitos    CHECK (telefone_ddd ~ '^[0-9]{2}$'),
    CONSTRAINT ck_cliente_numero_digitos CHECK (telefone_numero ~ '^[0-9]{8,9}$')
);

-- Índice funcional para a busca por nome do painel administrativo (RF0024).
-- Em lower() porque a consulta é case-insensitive; sem isso o índice não
-- seria usado e a busca varreria a tabela inteira.
CREATE INDEX ix_cliente_nome ON cliente (lower(nome));

COMMENT ON TABLE  cliente                 IS 'Dados cadastrais obrigatórios do cliente (RN0026)';
COMMENT ON COLUMN cliente.codigo          IS 'Código único no formato CLI-0001 (RNF0035)';
COMMENT ON COLUMN cliente.cpf             IS 'Somente dígitos; a máscara é responsabilidade da tela';
COMMENT ON COLUMN cliente.telefone_tipo   IS 'Telefone é composto por tipo, DDD e número (RN0026)';
