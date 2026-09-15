-- Endereços do cliente (RN0023, RF0026).
--
-- `entrega` e `cobranca` são booleanos independentes, e não um enum de
-- tipo, porque na prática o mesmo endereço costuma servir aos dois fins.
-- Com enum seria necessário duplicar a linha para satisfazer a RN0021 e a
-- RN0022 ao mesmo tempo.

CREATE TABLE endereco (
    id              BIGSERIAL    NOT NULL,
    cliente_id      BIGINT       NOT NULL,
    apelido         VARCHAR(40)  NOT NULL,
    tipo_residencia VARCHAR(20)  NOT NULL,
    tipo_logradouro VARCHAR(20)  NOT NULL,
    logradouro      VARCHAR(150) NOT NULL,
    numero          VARCHAR(10)  NOT NULL,
    complemento     VARCHAR(60),
    bairro          VARCHAR(80)  NOT NULL,
    cep             VARCHAR(8)   NOT NULL,
    cidade          VARCHAR(80)  NOT NULL,
    estado          VARCHAR(2)   NOT NULL,
    pais            VARCHAR(60)  NOT NULL DEFAULT 'Brasil',
    observacoes     VARCHAR(255),
    entrega         BOOLEAN      NOT NULL DEFAULT TRUE,
    cobranca        BOOLEAN      NOT NULL DEFAULT FALSE,
    principal       BOOLEAN      NOT NULL DEFAULT FALSE,

    CONSTRAINT pk_endereco         PRIMARY KEY (id),
    CONSTRAINT fk_endereco_cliente FOREIGN KEY (cliente_id) REFERENCES cliente (id),

    CONSTRAINT ck_endereco_tipo_residencia CHECK (tipo_residencia IN ('CASA', 'APARTAMENTO', 'CONDOMINIO', 'CHACARA', 'OUTRO')),
    CONSTRAINT ck_endereco_tipo_logradouro CHECK (tipo_logradouro IN ('RUA', 'AVENIDA', 'TRAVESSA', 'RODOVIA', 'ALAMEDA', 'PRACA', 'ESTRADA', 'OUTRO')),
    CONSTRAINT ck_endereco_cep             CHECK (cep ~ '^[0-9]{8}$'),
    CONSTRAINT ck_endereco_estado          CHECK (estado ~ '^[A-Z]{2}$'),

    -- Um endereço precisa servir a alguma finalidade.
    CONSTRAINT ck_endereco_uso CHECK (entrega OR cobranca),

    -- Só endereço de entrega pode ser o principal do checkout.
    CONSTRAINT ck_endereco_principal_entrega CHECK (NOT principal OR entrega)
);

CREATE INDEX ix_endereco_cliente ON endereco (cliente_id);

-- Um único endereço principal por cliente. Índice parcial em vez de regra
-- só no service: a garantia no service depende de nenhum outro caminho de
-- escrita existir, e sempre acaba existindo.
CREATE UNIQUE INDEX ux_endereco_principal ON endereco (cliente_id) WHERE principal;

COMMENT ON TABLE  endereco             IS 'Endereços de entrega e cobrança do cliente (RN0021, RN0022, RN0023)';
COMMENT ON COLUMN endereco.apelido     IS 'Nome curto que identifica o endereço, ex.: Casa, Trabalho (RF0026)';
COMMENT ON COLUMN endereco.observacoes IS 'Único campo opcional previsto pela RN0023';
COMMENT ON COLUMN endereco.cep         IS 'Somente dígitos; a máscara é responsabilidade da tela';
COMMENT ON COLUMN endereco.principal   IS 'Endereço de entrega sugerido por padrão no checkout';
