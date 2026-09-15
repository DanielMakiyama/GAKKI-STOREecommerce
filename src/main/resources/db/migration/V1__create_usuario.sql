-- Credencial de acesso ao sistema.
--
-- Está separada de `cliente` porque administrador e gerente de vendas
-- autenticam sem possuir dados cadastrais de cliente. Numa tabela única,
-- CPF e data de nascimento teriam de aceitar NULL para caber esses perfis,
-- e a obrigatoriedade exigida pela RN0026 deixaria de ser garantida pelo
-- banco.

CREATE TABLE usuario (
    id            BIGSERIAL    NOT NULL,
    email         VARCHAR(150) NOT NULL,
    senha         VARCHAR(60)  NOT NULL,
    papel         VARCHAR(20)  NOT NULL,
    ativo         BOOLEAN      NOT NULL DEFAULT TRUE,
    criado_em     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    atualizado_em TIMESTAMPTZ,

    CONSTRAINT pk_usuario       PRIMARY KEY (id),
    CONSTRAINT uk_usuario_email UNIQUE (email),
    CONSTRAINT ck_usuario_papel CHECK (papel IN ('CLIENTE', 'ADMINISTRADOR', 'GERENTE_VENDAS'))
);

COMMENT ON TABLE  usuario        IS 'Credenciais e perfil de acesso (RNF0033)';
COMMENT ON COLUMN usuario.email  IS 'Identificador de login. Gravado sempre em minúsculas pelo service';
COMMENT ON COLUMN usuario.senha  IS 'Hash BCrypt, sempre 60 caracteres (RNF0033)';
COMMENT ON COLUMN usuario.ativo  IS 'FALSE = cadastro inativado; o registro nunca é excluído (RF0023)';
