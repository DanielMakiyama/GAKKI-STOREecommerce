-- ============================================================
-- Massa de demonstração (RNF0013)
--
-- Numerada como V999 para ficar sempre depois das migrations de
-- estrutura, mesmo quando novas tabelas forem acrescentadas: V6 e V7
-- entram antes dela sem renumerar nada.
--
-- SENHAS (BCrypt, RNF0033):
--   cliente@gakkistore.com    Cliente@2026
--   admin@gakkistore.com      Admin@2026
--   os demais clientes        Senha@2026
--
-- Os dois primeiros e-mails são os que a tela de login do front usa nos
-- cartões de perfil — por isso precisam existir com estes endereços
-- exatos.
-- ============================================================

-- ------------------------------------------------------------
-- Usuários
-- ------------------------------------------------------------
INSERT INTO usuario (email, senha, papel, ativo) VALUES
  ('cliente@gakkistore.com',   '$2b$10$WRYPBKQjKxEzXdOyu3jBP.T1/ZXfi1zuJiDO9IqwBG69IwhnAYf42', 'CLIENTE',       TRUE),
  ('admin@gakkistore.com',     '$2b$10$m4f.6DXG/7LJS7rtFDcXnulz8Pyk9vDuMU3624gOAounSeTdGaCx.', 'ADMINISTRADOR', TRUE),
  ('maria.souza@example.com',  '$2b$10$m66Kspniko4ebOfprGQ0GuEP0HxmE3w/9KNwDhOa11Z6oydAmLApK', 'CLIENTE',       TRUE),
  ('joao.pereira@example.com', '$2b$10$aADwIyArl9MByKPXNabtHOd8WewYKZYi0S0iAz8peyCZWLR9o8WL.', 'CLIENTE',       TRUE),
  -- Cadastro inativo para demonstrar o filtro ativo=false da RF0024 e a
  -- recusa de login da RF0023 sem precisar inativar ninguém ao vivo.
  ('beatriz.lima@example.com', '$2b$10$SmBvizRSj.tswK9D/CJz4uYaWM8pmSkJ4ScAjIwyiH6DdOkOi464G', 'CLIENTE',       FALSE);

-- ------------------------------------------------------------
-- Clientes (RN0026)
--
-- O administrador NÃO recebe linha aqui: ele não tem CPF nem data de
-- nascimento, e é justamente por isso que `usuario` e `cliente` são
-- tabelas separadas.
--
-- Os CPFs abaixo têm dígitos verificadores válidos — o @CPF do Hibernate
-- Validator recusaria qualquer sequência inventada.
-- ------------------------------------------------------------
INSERT INTO cliente (usuario_id, codigo, nome, cpf, genero, data_nascimento,
                     telefone_tipo, telefone_ddd, telefone_numero)
VALUES
  ((SELECT id FROM usuario WHERE email = 'cliente@gakkistore.com'),
   'CLI-0001', 'Cliente Demonstração', '27726568100', 'NAO_INFORMADO', '1995-03-12',
   'CELULAR', '11', '988887777'),

  ((SELECT id FROM usuario WHERE email = 'maria.souza@example.com'),
   'CLI-0002', 'Maria Souza', '29528926444', 'FEMININO', '1988-07-24',
   'CELULAR', '19', '977776666'),

  ((SELECT id FROM usuario WHERE email = 'joao.pereira@example.com'),
   'CLI-0003', 'João Pereira', '73191675140', 'MASCULINO', '2001-11-05',
   'RESIDENCIAL', '13', '33334444'),

  ((SELECT id FROM usuario WHERE email = 'beatriz.lima@example.com'),
   'CLI-0004', 'Beatriz Lima', '03365020284', 'FEMININO', '1999-01-30',
   'COMERCIAL', '11', '955554444');

-- A sequence precisa saber que 4 códigos já foram usados. Sem isso, o
-- próximo cadastro pela API geraria CLI-0001 e colidiria com a UNIQUE.
SELECT setval('seq_codigo_cliente', 4);

-- ------------------------------------------------------------
-- Endereços (RN0021, RN0022, RN0023)
--
-- O cliente de demonstração recebe DOIS endereços: um servindo entrega e
-- cobrança (principal) e outro só de entrega. Com isso a suíte de testes
-- tem estado inicial para exercitar a troca de principal e a recusa de
-- remoção do último de cobrança, sem cadastrar nada antes.
--
-- Os demais recebem um endereço servindo aos dois fins — o mínimo que a
-- RN0021 e a RN0022 exigem.
-- ------------------------------------------------------------
INSERT INTO endereco (cliente_id, apelido, tipo_residencia, tipo_logradouro, logradouro,
                      numero, complemento, bairro, cep, cidade, estado, pais,
                      observacoes, entrega, cobranca, principal)
VALUES
  ((SELECT id FROM cliente WHERE codigo = 'CLI-0001'),
   'Casa', 'APARTAMENTO', 'RUA', 'das Guitarras', '123', 'Apto 42',
   'Centro', '01000000', 'São Paulo', 'SP', 'Brasil', NULL, TRUE, TRUE, TRUE),

  ((SELECT id FROM cliente WHERE codigo = 'CLI-0001'),
   'Trabalho', 'OUTRO', 'AVENIDA', 'Paulista', '1000', 'Conjunto 12',
   'Bela Vista', '01310100', 'São Paulo', 'SP', 'Brasil',
   'Entregar na portaria', TRUE, FALSE, FALSE),

  ((SELECT id FROM cliente WHERE codigo = 'CLI-0002'),
   'Casa', 'CASA', 'AVENIDA', 'Central', '500', NULL,
   'Cambuí', '13025000', 'Campinas', 'SP', 'Brasil', NULL, TRUE, TRUE, TRUE),

  ((SELECT id FROM cliente WHERE codigo = 'CLI-0003'),
   'Casa', 'CONDOMINIO', 'RUA', 'das Palmeiras', '44', 'Bloco B',
   'Gonzaga', '11055000', 'Santos', 'SP', 'Brasil', NULL, TRUE, TRUE, TRUE),

  ((SELECT id FROM cliente WHERE codigo = 'CLI-0004'),
   'Casa', 'APARTAMENTO', 'TRAVESSA', 'dos Sinos', '7', NULL,
   'Vila Mariana', '04101000', 'São Paulo', 'SP', 'Brasil', NULL, TRUE, TRUE, TRUE);

-- ------------------------------------------------------------
-- Cartões (RN0024, RN0025, RF0027)
--
-- Apenas os quatro últimos dígitos, como em produção. O cliente de
-- demonstração recebe dois, para a troca de preferencial ter o que
-- trocar.
--
-- As bandeiras vêm por nome, resolvidas contra a tabela semeada na V3 —
-- se alguém renomear uma bandeira, o seed falha em vez de gravar um id
-- errado.
-- ------------------------------------------------------------
INSERT INTO cartao (cliente_id, bandeira_id, apelido, ultimos_digitos,
                    nome_titular, validade_mes, validade_ano, preferencial)
VALUES
  ((SELECT id FROM cliente WHERE codigo = 'CLI-0001'),
   (SELECT id FROM bandeira WHERE nome = 'Visa'),
   'Cartão principal', '4321', 'CLIENTE DEMONSTRACAO', 12, 2029, TRUE),

  ((SELECT id FROM cliente WHERE codigo = 'CLI-0001'),
   (SELECT id FROM bandeira WHERE nome = 'Mastercard'),
   'Cartão reserva', '9876', 'CLIENTE DEMONSTRACAO', 6, 2028, FALSE),

  ((SELECT id FROM cliente WHERE codigo = 'CLI-0002'),
   (SELECT id FROM bandeira WHERE nome = 'Elo'),
   'Cartão de Maria', '9911', 'MARIA SOUZA', 3, 2030, TRUE);
