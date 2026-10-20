-- Bloqueio temporário do item no carrinho e sua expiração
-- (RN0044, RN0045, RNF0042).
--
-- O prazo vive em `carrinho.bloqueado_ate`, coluna criada na V9 e até
-- agora sem uso. É UM prazo por carrinho, reiniciado a cada item
-- incluído, e não um prazo por item: é a leitura que a observação da
-- própria RN0044 impõe — "o prazo parametrizado deve ser relativo ao
-- último item incluído no carrinho". A ambiguidade entre as duas
-- leituras está registrada na seção 16.2 da Especificação de Caso de Uso.
--
-- Ao expirar, os itens saem de `item_carrinho` e passam para cá. Podiam
-- ficar lá com uma coluna `retirado_em`, mas aí toda consulta de
-- carrinho — total, frete, finalização — passaria a precisar filtrar
-- pelo flag, e um esquecimento cobraria por item que já não existe.
-- Separados, `carrinho.itens` continua significando "itens ativos" em
-- todo o código que já existe.

CREATE TABLE item_retirado (
    id             BIGSERIAL     NOT NULL,
    carrinho_id    BIGINT        NOT NULL,
    instrumento_id BIGINT        NOT NULL,
    quantidade     INTEGER       NOT NULL,
    valor_unitario NUMERIC(12,2) NOT NULL,
    retirado_em    TIMESTAMPTZ   NOT NULL DEFAULT now(),

    CONSTRAINT pk_item_retirado             PRIMARY KEY (id),
    CONSTRAINT fk_item_retirado_carrinho    FOREIGN KEY (carrinho_id)    REFERENCES carrinho (id),
    CONSTRAINT fk_item_retirado_instrumento FOREIGN KEY (instrumento_id) REFERENCES instrumento (id),

    CONSTRAINT ck_item_retirado_quantidade CHECK (quantidade > 0)
);

CREATE INDEX ix_item_retirado_carrinho ON item_retirado (carrinho_id);

COMMENT ON TABLE  item_retirado                IS 'Itens removidos do carrinho por expiração do bloqueio, exibidos pela RNF0042';
COMMENT ON COLUMN item_retirado.valor_unitario IS 'Preço no momento da adição; preservado para o cliente ver o que perdeu ao expirar';

-- Carrinhos que já existem nasceram sem prazo. Marcá-los como expirados
-- agora é o único estado honesto: ninguém iniciou contagem para eles, e
-- deixar nulo os faria bloquear estoque para sempre.
UPDATE carrinho SET bloqueado_ate = now() WHERE bloqueado_ate IS NULL;

COMMENT ON COLUMN carrinho.bloqueado_ate IS 'Fim do prazo de bloqueio, reiniciado a cada item incluído (RN0044)';
