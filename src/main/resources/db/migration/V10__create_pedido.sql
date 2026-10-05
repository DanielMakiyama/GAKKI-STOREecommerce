-- Pedido e itens do pedido (RF0038).
--
-- O pagamento fica numa migration à parte (V12), depois de `cupom` (V11)
-- existir — `pagamento` referencia as duas tabelas por FK, então precisa
-- vir depois de ambas.
--
-- Os valores de `status` seguem literalmente o texto do DRS, e não os
-- nomes já usados no protótipo front-end mockado (EM_ABERTO,
-- PAGAMENTO_REALIZADO, EM_TRANSITO, CANCELADA). O DRS é a única fonte
-- válida para a especificação do caso de uso; o mock e o front são
-- ajustados depois para bater com o backend, não o contrário (contrato de
-- Vendas v3, observação 3 da seção 10).
--
-- O enunciado da atividade de criação de pedido exclui explicitamente a
-- validação de pagamento (RN0037/RN0038): o happy path desta fase sempre
-- grava EM_PROCESSAMENTO, nunca decide aprovação. Os demais valores do
-- enum já entram no domínio para não exigir migration nova quando a
-- validação de pagamento, o despacho e a troca forem implementados.

CREATE SEQUENCE seq_numero_pedido START WITH 1 INCREMENT BY 1;

CREATE TABLE pedido (
    id                    BIGSERIAL     NOT NULL,
    numero                VARCHAR(20)   NOT NULL,
    cliente_id            BIGINT        NOT NULL,
    endereco_entrega_id   BIGINT        NOT NULL,
    status                VARCHAR(20)   NOT NULL,
    valor_frete           NUMERIC(12,2) NOT NULL,
    valor_total           NUMERIC(12,2) NOT NULL,
    criado_em             TIMESTAMPTZ   NOT NULL DEFAULT now(),

    CONSTRAINT pk_pedido                  PRIMARY KEY (id),
    CONSTRAINT fk_pedido_cliente          FOREIGN KEY (cliente_id)          REFERENCES cliente (id),
    CONSTRAINT fk_pedido_endereco_entrega FOREIGN KEY (endereco_entrega_id) REFERENCES endereco (id),
    CONSTRAINT uk_pedido_numero           UNIQUE (numero),

    CONSTRAINT ck_pedido_status CHECK (status IN (
        'EM_PROCESSAMENTO', 'APROVADA', 'REPROVADA', 'EM_TRANSPORTE',
        'ENTREGUE', 'EM_TROCA', 'TROCA_AUTORIZADA', 'TROCADO'
    )),
    CONSTRAINT ck_pedido_valor_frete_nao_negativo CHECK (valor_frete >= 0),
    CONSTRAINT ck_pedido_valor_total_nao_negativo CHECK (valor_total >= 0)
);

CREATE INDEX ix_pedido_cliente ON pedido (cliente_id);

COMMENT ON TABLE  pedido         IS 'Pedido de compra e seu ciclo de vida de status (RF0038)';
COMMENT ON COLUMN pedido.numero  IS 'Número único no formato PED-00001';
COMMENT ON COLUMN pedido.status  IS 'Ciclo de vida completo do DRS; esta fatia só grava EM_PROCESSAMENTO — RN0037/RN0038 (aprovação) fora de escopo';

CREATE TABLE item_pedido (
    id                BIGSERIAL     NOT NULL,
    pedido_id         BIGINT        NOT NULL,
    instrumento_id    BIGINT        NOT NULL,
    instrumento_nome  VARCHAR(150)  NOT NULL,
    quantidade        INTEGER       NOT NULL,
    valor_unitario    NUMERIC(12,2) NOT NULL,
    em_troca          BOOLEAN       NOT NULL DEFAULT FALSE,

    CONSTRAINT pk_item_pedido               PRIMARY KEY (id),
    CONSTRAINT fk_item_pedido_pedido        FOREIGN KEY (pedido_id)      REFERENCES pedido (id),
    CONSTRAINT fk_item_pedido_instrumento   FOREIGN KEY (instrumento_id) REFERENCES instrumento (id),

    CONSTRAINT ck_item_pedido_quantidade CHECK (quantidade > 0)
);

CREATE INDEX ix_item_pedido_pedido ON item_pedido (pedido_id);

COMMENT ON TABLE  item_pedido                  IS 'Itens do pedido, fotografados no momento da compra (RF0038)';
COMMENT ON COLUMN item_pedido.instrumento_nome IS 'Fotografia do nome do instrumento; preserva o histórico mesmo se o cadastro mudar depois';
COMMENT ON COLUMN item_pedido.em_troca         IS 'Reservado para o processo de troca (RF0041 em diante); sem uso nesta fatia';
