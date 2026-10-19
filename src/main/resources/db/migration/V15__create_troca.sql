-- Pedido de troca e notificação ao cliente (RF0041 a RF0045, RN0046).
--
-- A troca é entidade própria, e não apenas mais um status no pedido,
-- porque a RN0041 fala em "gerar um pedido de troca" para os itens
-- selecionados: ela tem ciclo de vida, valor e cupom próprios, e uma
-- compra entregue pode gerar mais de uma ao longo do tempo. O status no
-- `pedido` continua existindo e é espelhado apenas no caso que a própria
-- RN0041 destaca — quando o cliente troca a compra inteira.
--
-- Os três valores de status são os do DRS. O protótipo do front
-- trabalhava com seis (TROCA_SOLICITADA, ITEM_ENVIADO, ITEM_RECEBIDO e
-- companhia); vale a mesma regra já adotada no status do pedido, de que
-- o DRS é a fonte da especificação e o front se ajusta ao backend.
--
-- Não há estado de recusa: a RF0042 descreve só o caminho da
-- autorização, e o DRS não prevê estado nem regra para a negativa.
-- Lacuna registrada na seção 16.2 da Especificação de Caso de Uso.

CREATE SEQUENCE seq_numero_troca START WITH 1 INCREMENT BY 1;

COMMENT ON SEQUENCE seq_numero_troca IS 'Numeração dos pedidos de troca, no formato TRC-00001';

CREATE TABLE troca (
    id              BIGSERIAL     NOT NULL,
    numero          VARCHAR(20)   NOT NULL,
    pedido_id       BIGINT        NOT NULL,
    cliente_id      BIGINT        NOT NULL,
    status          VARCHAR(20)   NOT NULL,
    valor           NUMERIC(12,2) NOT NULL,
    retorna_estoque BOOLEAN,
    cupom_id        BIGINT,
    criado_em       TIMESTAMPTZ   NOT NULL DEFAULT now(),
    atualizado_em   TIMESTAMPTZ,

    CONSTRAINT pk_troca         PRIMARY KEY (id),
    CONSTRAINT fk_troca_pedido  FOREIGN KEY (pedido_id)  REFERENCES pedido (id),
    CONSTRAINT fk_troca_cliente FOREIGN KEY (cliente_id) REFERENCES cliente (id),
    CONSTRAINT fk_troca_cupom   FOREIGN KEY (cupom_id)   REFERENCES cupom (id),
    CONSTRAINT uk_troca_numero  UNIQUE (numero),

    CONSTRAINT ck_troca_status CHECK (status IN ('EM_TROCA', 'TROCA_AUTORIZADA', 'TROCADO')),
    CONSTRAINT ck_troca_valor  CHECK (valor > 0),

    -- `retorna_estoque` e `cupom_id` só passam a existir na confirmação
    -- de recebimento (RF0044, RF0045). Antes disso são nulos; depois, o
    -- cupom é obrigatório — é o que a RF0045 garante ao cliente.
    CONSTRAINT ck_troca_conclusao CHECK (
        status <> 'TROCADO' OR (retorna_estoque IS NOT NULL AND cupom_id IS NOT NULL)
    )
);

CREATE INDEX ix_troca_cliente ON troca (cliente_id);

-- RF0043 — o administrador consulta a fila pelo status.
CREATE INDEX ix_troca_status ON troca (status);

COMMENT ON TABLE  troca                 IS 'Pedido de troca de itens de uma compra entregue (RF0041, RN0041)';
COMMENT ON COLUMN troca.valor           IS 'Soma dos itens trocados; vira o valor do cupom de troca (RF0045)';
COMMENT ON COLUMN troca.retorna_estoque IS 'Decisão do administrador na confirmação de recebimento (RF0044); nulo até lá';
COMMENT ON COLUMN troca.cupom_id        IS 'Cupom de troca emitido ao concluir (RF0045); nulo até lá';

CREATE TABLE item_troca (
    id             BIGSERIAL NOT NULL,
    troca_id       BIGINT    NOT NULL,
    item_pedido_id BIGINT    NOT NULL,
    quantidade     INTEGER   NOT NULL,

    CONSTRAINT pk_item_troca             PRIMARY KEY (id),
    CONSTRAINT fk_item_troca_troca       FOREIGN KEY (troca_id)       REFERENCES troca (id),
    CONSTRAINT fk_item_troca_item_pedido FOREIGN KEY (item_pedido_id) REFERENCES item_pedido (id),

    CONSTRAINT ck_item_troca_quantidade CHECK (quantidade > 0)
);

-- O mesmo item do pedido entra uma única vez em cada troca.
--
-- Que ele não entre em DUAS trocas diferentes é garantido de outro
-- jeito: a coluna item_pedido.em_troca, que o service marca ao
-- solicitar e consulta antes de aceitar uma nova solicitação. Um índice
-- não expressaria essa regra sem replicar o estado aqui.
CREATE UNIQUE INDEX ux_item_troca_item ON item_troca (troca_id, item_pedido_id);

CREATE INDEX ix_item_troca_troca ON item_troca (troca_id);

COMMENT ON TABLE item_troca IS 'Itens da compra incluídos no pedido de troca (RF0041)';

-- RN0046 — notificação ao cliente quando a troca é autorizada.
--
-- Tabela mínima e deliberadamente genérica: a regra pede que o sistema
-- "gere uma notificação", sem definir canal, formato ou leitura. Um
-- registro consultável pelo cliente atende o requisito sem inventar
-- e-mail, push ou qualquer canal que o DRS não menciona.
CREATE TABLE notificacao (
    id         BIGSERIAL    NOT NULL,
    cliente_id BIGINT       NOT NULL,
    mensagem   VARCHAR(255) NOT NULL,
    lida       BOOLEAN      NOT NULL DEFAULT FALSE,
    criado_em  TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT pk_notificacao         PRIMARY KEY (id),
    CONSTRAINT fk_notificacao_cliente FOREIGN KEY (cliente_id) REFERENCES cliente (id)
);

CREATE INDEX ix_notificacao_cliente ON notificacao (cliente_id, lida);

COMMENT ON TABLE notificacao IS 'Notificações geradas ao cliente; nesta fase, a autorização de troca (RN0046)';
