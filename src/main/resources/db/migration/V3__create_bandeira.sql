-- Bandeiras aceitas para cartão de crédito.
--
-- A RN0025 exige que todo cartão seja "de alguma bandeira registrada no
-- sistema" — isto é, um cadastro de domínio, não uma constante no código.
-- Seed na própria migration atende a RNF0013.

CREATE TABLE bandeira (
    id    BIGSERIAL   NOT NULL,
    nome  VARCHAR(40) NOT NULL,
    ativo BOOLEAN     NOT NULL DEFAULT TRUE,

    CONSTRAINT pk_bandeira       PRIMARY KEY (id),
    CONSTRAINT uk_bandeira_nome  UNIQUE (nome)
);

INSERT INTO bandeira (nome) VALUES
    ('Visa'),
    ('Mastercard'),
    ('Elo'),
    ('American Express'),
    ('Hipercard'),
    ('Diners Club');

COMMENT ON TABLE  bandeira       IS 'Domínio de bandeiras permitidas (RN0025)';
COMMENT ON COLUMN bandeira.ativo IS 'Bandeira desativada não pode receber novos cartões, mas preserva os já cadastrados';
