-- Chatbot de recomendação com IA generativa (RNF0044).

-- ------------------------------------------------------------
-- Preferências do cliente (critério e)
-- Informadas pelo próprio cliente num formulário do perfil: nível,
-- orçamento, categorias de interesse e estilos musicais.
-- ------------------------------------------------------------
CREATE TABLE preferencia_cliente (
    id               BIGSERIAL     NOT NULL,
    cliente_id       BIGINT        NOT NULL,
    nivel            VARCHAR(20)   NOT NULL,
    orcamento_maximo NUMERIC(12,2),
    observacoes      VARCHAR(300),
    criado_em        TIMESTAMPTZ   NOT NULL DEFAULT now(),
    atualizado_em    TIMESTAMPTZ,
    CONSTRAINT pk_preferencia_cliente          PRIMARY KEY (id),
    CONSTRAINT fk_preferencia_cliente_cliente  FOREIGN KEY (cliente_id) REFERENCES cliente (id),
    CONSTRAINT uk_preferencia_cliente_cliente  UNIQUE (cliente_id),
    CONSTRAINT ck_preferencia_cliente_nivel    CHECK (nivel IN ('INICIANTE', 'INTERMEDIARIO', 'AVANCADO')),
    CONSTRAINT ck_preferencia_cliente_orcamento CHECK (orcamento_maximo IS NULL OR orcamento_maximo > 0)
);

CREATE TABLE preferencia_cliente_categoria (
    preferencia_id BIGINT NOT NULL,
    categoria_id   BIGINT NOT NULL,
    CONSTRAINT pk_preferencia_cliente_categoria             PRIMARY KEY (preferencia_id, categoria_id),
    CONSTRAINT fk_preferencia_cliente_categoria_preferencia FOREIGN KEY (preferencia_id) REFERENCES preferencia_cliente (id) ON DELETE CASCADE,
    CONSTRAINT fk_preferencia_cliente_categoria_categoria   FOREIGN KEY (categoria_id)   REFERENCES categoria (id)
);

CREATE TABLE preferencia_cliente_estilo (
    preferencia_id BIGINT      NOT NULL,
    estilo         VARCHAR(20) NOT NULL,
    CONSTRAINT pk_preferencia_cliente_estilo             PRIMARY KEY (preferencia_id, estilo),
    CONSTRAINT fk_preferencia_cliente_estilo_preferencia FOREIGN KEY (preferencia_id) REFERENCES preferencia_cliente (id) ON DELETE CASCADE,
    CONSTRAINT ck_preferencia_cliente_estilo CHECK (estilo IN (
        'ROCK', 'BLUES', 'METAL', 'JAZZ', 'POP', 'MPB', 'FUNK',
        'SERTANEJO', 'GOSPEL', 'ELETRONICA', 'CLASSICA'))
);

-- ------------------------------------------------------------
-- Versões do modelo de IA (critério c, item 8 da apresentação)
-- Cada ajuste fino gera uma versão. Só uma fica ativa e atende o chat.
-- dados_ate marca até quando as vendas e o feedback entraram no
-- treinamento: o que chegar depois entra na próxima versão.
-- ------------------------------------------------------------
CREATE TABLE modelo_ia (
    id                BIGSERIAL     NOT NULL,
    versao            INTEGER       NOT NULL,
    provedor          VARCHAR(30)   NOT NULL,
    modelo_base       VARCHAR(100)  NOT NULL,
    identificador     VARCHAR(300),
    job_id            VARCHAR(300),
    status            VARCHAR(20)   NOT NULL,
    ativo             BOOLEAN       NOT NULL DEFAULT FALSE,
    exemplos_total    INTEGER       NOT NULL DEFAULT 0,
    exemplos_vendas   INTEGER       NOT NULL DEFAULT 0,
    exemplos_feedback INTEGER       NOT NULL DEFAULT 0,
    dados_ate         TIMESTAMPTZ   NOT NULL,
    iniciado_em       TIMESTAMPTZ   NOT NULL DEFAULT now(),
    concluido_em      TIMESTAMPTZ,
    erro              VARCHAR(1000),
    CONSTRAINT pk_modelo_ia        PRIMARY KEY (id),
    CONSTRAINT uk_modelo_ia_versao UNIQUE (versao),
    CONSTRAINT ck_modelo_ia_status CHECK (status IN ('PREPARANDO', 'TREINANDO', 'PRONTO', 'FALHOU')),
    CONSTRAINT ck_modelo_ia_ativo_pronto CHECK (NOT ativo OR status = 'PRONTO')
);

CREATE UNIQUE INDEX ux_modelo_ia_ativo ON modelo_ia (ativo) WHERE ativo;

-- ------------------------------------------------------------
-- Conversas e mensagens do chat (critério a: só o perfil cliente)
-- ------------------------------------------------------------
CREATE TABLE conversa_chat (
    id          BIGSERIAL   NOT NULL,
    cliente_id  BIGINT      NOT NULL,
    iniciada_em TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT pk_conversa_chat         PRIMARY KEY (id),
    CONSTRAINT fk_conversa_chat_cliente FOREIGN KEY (cliente_id) REFERENCES cliente (id)
);

CREATE INDEX ix_conversa_chat_cliente ON conversa_chat (cliente_id, iniciada_em);

-- modelo_ia_id registra qual versão do modelo deu cada resposta.
-- Nulo nas mensagens do cliente e nas anteriores ao primeiro ajuste fino.
CREATE TABLE mensagem_chat (
    id           BIGSERIAL   NOT NULL,
    conversa_id  BIGINT      NOT NULL,
    autor        VARCHAR(12) NOT NULL,
    texto        TEXT        NOT NULL,
    modelo_ia_id BIGINT,
    criado_em    TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT pk_mensagem_chat           PRIMARY KEY (id),
    CONSTRAINT fk_mensagem_chat_conversa  FOREIGN KEY (conversa_id)  REFERENCES conversa_chat (id),
    CONSTRAINT fk_mensagem_chat_modelo_ia FOREIGN KEY (modelo_ia_id) REFERENCES modelo_ia (id),
    CONSTRAINT ck_mensagem_chat_autor     CHECK (autor IN ('CLIENTE', 'ASSISTENTE')),
    CONSTRAINT ck_mensagem_chat_texto     CHECK (length(texto) BETWEEN 1 AND 4000),
    CONSTRAINT ck_mensagem_chat_modelo    CHECK (autor = 'ASSISTENTE' OR modelo_ia_id IS NULL)
);

CREATE INDEX ix_mensagem_chat_conversa ON mensagem_chat (conversa_id, criado_em);

-- ------------------------------------------------------------
-- Feedback das respostas (critério d)
-- Uma avaliação por resposta do assistente; o cliente pode mudá-la.
-- ------------------------------------------------------------
CREATE TABLE feedback_resposta (
    id            BIGSERIAL   NOT NULL,
    mensagem_id   BIGINT      NOT NULL,
    cliente_id    BIGINT      NOT NULL,
    avaliacao     VARCHAR(10) NOT NULL,
    criado_em     TIMESTAMPTZ NOT NULL DEFAULT now(),
    atualizado_em TIMESTAMPTZ,
    CONSTRAINT pk_feedback_resposta          PRIMARY KEY (id),
    CONSTRAINT fk_feedback_resposta_mensagem FOREIGN KEY (mensagem_id) REFERENCES mensagem_chat (id),
    CONSTRAINT fk_feedback_resposta_cliente  FOREIGN KEY (cliente_id)  REFERENCES cliente (id),
    CONSTRAINT uk_feedback_resposta_mensagem UNIQUE (mensagem_id),
    CONSTRAINT ck_feedback_resposta_avaliacao CHECK (avaliacao IN ('UTIL', 'NAO_UTIL'))
);

-- O treinamento busca o feedback novo por data (dados_ate da versão anterior).
CREATE INDEX ix_feedback_resposta_data ON feedback_resposta (coalesce(atualizado_em, criado_em));
