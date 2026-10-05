-- ============================================================
-- Massa de demonstração (RNF0013)
--
-- Numerada como V999 para ficar sempre depois das migrations de
-- estrutura, mesmo quando novas tabelas forem acrescentadas: V6 a V14
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
--
-- A partir da fatia de Vendas, este seed também monta os dados dos sete
-- casos do roteiro da apresentação. Os valores NÃO são decorativos: estão
-- calculados para reproduzir o exemplo numérico da RN0036 do DRS. Ver a
-- seção "Cenários da apresentação", no fim do arquivo, antes de mudar
-- qualquer preço.
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
--
-- Todos em SP de propósito: a fórmula de frete (RF0034) cobra adicional
-- fora de SP, e os valores dos cenários abaixo contam com o frete sem
-- esse adicional. Para demonstrar o adicional, cadastre um endereço de
-- outro estado durante a apresentação — é o caso 3 do roteiro.
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
-- trocar — e para o caso 4 do roteiro, pagamento com mais de um cartão.
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

-- ============================================================
-- MÓDULO DE VENDAS
-- ============================================================

-- ------------------------------------------------------------
-- Domínio do catálogo (RNF0013)
-- ------------------------------------------------------------
INSERT INTO categoria (nome) VALUES
  ('Guitarras'), ('Teclados'), ('Pedais'), ('Interfaces de Áudio'), ('Acessórios');

INSERT INTO fabricante (nome) VALUES
  ('Fender'), ('Yamaha'), ('Boss'), ('Focusrite'), ('Elixir');

INSERT INTO fornecedor (nome) VALUES
  ('Distribuidora Musical Brasil');

-- Margem única de 40% para todo o catálogo do seed. Com ela, a conta da
-- RN0051 fecha redonda: custo × 1,40 = valor de venda, e os preços abaixo
-- são exatamente isso — não números escolhidos à toa.
INSERT INTO grupo_precificacao (nome, margem_percentual, ativo) VALUES
  ('Padrão 40%', 40.00, TRUE);

-- ------------------------------------------------------------
-- Instrumentos (RF0011)
--
-- `valor_venda` = custo do lote × 1,40, coerente com a margem acima e com
-- a RN0051 (maior custo registrado). Nesta fase ninguém recalcula nada:
-- a entrada em estoque é funcionalidade de outra fatia, então o seed já
-- grava o resultado.
-- ------------------------------------------------------------
INSERT INTO instrumento (codigo, nome, descricao, fabricante_id, grupo_precificacao_id,
                         ano_fabricacao, valor_venda, quantidade_estoque, ativo)
VALUES
  ('INST-0001', 'Guitarra Fender Player Stratocaster',
   'Corpo em alder, braço em maple, três captadores single-coil. Acompanha bag.',
   (SELECT id FROM fabricante WHERE nome = 'Fender'),
   (SELECT id FROM grupo_precificacao WHERE nome = 'Padrão 40%'),
   2024, 1400.00, 5, TRUE),

  ('INST-0002', 'Teclado Yamaha MX61',
   'Sintetizador de 61 teclas sensíveis ao toque, com banco de sons Motif XS.',
   (SELECT id FROM fabricante WHERE nome = 'Yamaha'),
   (SELECT id FROM grupo_precificacao WHERE nome = 'Padrão 40%'),
   2023, 2800.00, 3, TRUE),

  ('INST-0003', 'Pedal Boss DS-1 Distortion',
   'Pedal de distorção clássico, controles de tom, nível e distorção.',
   (SELECT id FROM fabricante WHERE nome = 'Boss'),
   (SELECT id FROM grupo_precificacao WHERE nome = 'Padrão 40%'),
   2025, 700.00, 8, TRUE),

  -- Estoque propositalmente baixo: com 2 unidades, pedir 3 no carrinho
  -- demonstra a recusa da RN0031 sem precisar preparar nada antes.
  ('INST-0004', 'Interface Focusrite Scarlett 2i2',
   'Interface de áudio USB de duas entradas, pré-amplificadores Scarlett.',
   (SELECT id FROM fabricante WHERE nome = 'Focusrite'),
   (SELECT id FROM grupo_precificacao WHERE nome = 'Padrão 40%'),
   2025, 1260.00, 2, TRUE),

  -- O item barato do catálogo, e a peça central dos cenários de cupom:
  -- R$ 35,00 + R$ 15,00 de frete = R$ 50,00 exatos. Ver a seção final.
  ('INST-0005', 'Jogo de Cordas Elixir Nanoweb .010',
   'Encordoamento para guitarra, calibre .010-.046, revestimento Nanoweb.',
   (SELECT id FROM fabricante WHERE nome = 'Elixir'),
   (SELECT id FROM grupo_precificacao WHERE nome = 'Padrão 40%'),
   2026, 35.00, 50, TRUE);

SELECT setval('seq_codigo_instrumento', 5);

-- Vínculo N:N (RN0012). O jogo de cordas aparece em duas categorias para
-- a tela de filtros ter um caso real de item multicategoria.
INSERT INTO instrumento_categoria (instrumento_id, categoria_id) VALUES
  ((SELECT id FROM instrumento WHERE codigo = 'INST-0001'), (SELECT id FROM categoria WHERE nome = 'Guitarras')),
  ((SELECT id FROM instrumento WHERE codigo = 'INST-0002'), (SELECT id FROM categoria WHERE nome = 'Teclados')),
  ((SELECT id FROM instrumento WHERE codigo = 'INST-0003'), (SELECT id FROM categoria WHERE nome = 'Pedais')),
  ((SELECT id FROM instrumento WHERE codigo = 'INST-0004'), (SELECT id FROM categoria WHERE nome = 'Interfaces de Áudio')),
  ((SELECT id FROM instrumento WHERE codigo = 'INST-0005'), (SELECT id FROM categoria WHERE nome = 'Acessórios')),
  ((SELECT id FROM instrumento WHERE codigo = 'INST-0005'), (SELECT id FROM categoria WHERE nome = 'Guitarras'));

-- ------------------------------------------------------------
-- Entrada em estoque (RF0051, RN0050)
--
-- Um lote por instrumento, com o custo que justifica o valor de venda
-- gravado acima. O enunciado da atividade pressupõe que o estoque já
-- exista na base — esta é essa entrada prévia.
-- ------------------------------------------------------------
INSERT INTO item_estoque (instrumento_id, fornecedor_id, quantidade, valor_custo, data_entrada)
VALUES
  ((SELECT id FROM instrumento WHERE codigo = 'INST-0001'),
   (SELECT id FROM fornecedor WHERE nome = 'Distribuidora Musical Brasil'), 5, 1000.00, '2026-09-01'),
  ((SELECT id FROM instrumento WHERE codigo = 'INST-0002'),
   (SELECT id FROM fornecedor WHERE nome = 'Distribuidora Musical Brasil'), 3, 2000.00, '2026-09-01'),
  ((SELECT id FROM instrumento WHERE codigo = 'INST-0003'),
   (SELECT id FROM fornecedor WHERE nome = 'Distribuidora Musical Brasil'), 8,  500.00, '2026-09-01'),
  ((SELECT id FROM instrumento WHERE codigo = 'INST-0004'),
   (SELECT id FROM fornecedor WHERE nome = 'Distribuidora Musical Brasil'), 2,  900.00, '2026-09-01'),
  ((SELECT id FROM instrumento WHERE codigo = 'INST-0005'),
   (SELECT id FROM fornecedor WHERE nome = 'Distribuidora Musical Brasil'), 50,  25.00, '2026-09-01');

-- ------------------------------------------------------------
-- Cupons (RF0037, RN0033, RN0036)
--
-- O enunciado autoriza carregar os cupons por seed, já que a geração pelo
-- fluxo de troca está fora do escopo desta fase.
--
-- Os três cupons de TROCA têm exatamente os valores do exemplo numérico
-- da RN0036 no DRS: R$ 20, R$ 40 e R$ 35. Alterá-los quebra a
-- correspondência com o documento — e é o que torna a demonstração
-- verificável contra o requisito, em vez de contra um número inventado.
--
-- Os códigos são fixos e de quatro dígitos; a sequence seq_codigo_cupom
-- (V14) começa em 1000, então os cupons de troco emitidos durante a
-- apresentação saem como TROCA-1000 em diante e nunca colidem com estes.
-- ------------------------------------------------------------
INSERT INTO cupom (codigo, tipo, cliente_id, valor, utilizado) VALUES
  ('TROCA-0001', 'TROCA',       (SELECT id FROM cliente WHERE codigo = 'CLI-0001'), 20.00, FALSE),
  ('TROCA-0002', 'TROCA',       (SELECT id FROM cliente WHERE codigo = 'CLI-0001'), 40.00, FALSE),
  ('TROCA-0003', 'TROCA',       (SELECT id FROM cliente WHERE codigo = 'CLI-0001'), 35.00, FALSE),
  -- Promocional de R$ 45 para o caso 5 do roteiro: numa compra de R$ 50
  -- ele deixa só R$ 5,00 para o cartão, abaixo do piso de R$ 10 — que é
  -- exatamente a exceção que a RN0035 abre.
  ('PROMO-0001', 'PROMOCIONAL', (SELECT id FROM cliente WHERE codigo = 'CLI-0001'), 45.00, FALSE);

-- ============================================================
-- Cenários da apresentação — conferir antes de mudar preços
--
-- Frete (RF0034, fórmula da decisão 10 do contrato de Vendas):
--   R$ 15,00 + R$ 5,00 × (peças − 1) + R$ 10,00 se o estado ≠ SP
-- Todos os endereços do seed são SP, então o adicional não entra.
--
-- 1) Mais de um item e alteração de quantidade (RF0031, RF0032)
--    Pedal R$ 700 + cordas R$ 35; mudar a quantidade das cordas para 3.
--    Frete com 4 peças: 15 + 5×3 = R$ 30,00.
--
-- 2) Compra com endereço e cartão já cadastrados (RF0033/0035/0036/0038)
--    Guitarra R$ 1.400 + frete R$ 15 = R$ 1.415,00 no Cartão principal.
--
-- 3) Endereço e cartão novos durante a compra (RN0023, RN0024, RN0025)
--    Cadastrar um endereço FORA de SP para o frete mostrar o adicional
--    de R$ 10,00 na prática.
--
-- 4) Mais de um cartão, piso de R$ 10 por cartão (RN0034)
--    Guitarra: total R$ 1.415,00 → R$ 1.000,00 no principal e
--    R$ 415,00 no reserva. Ambos acima do piso.
--
-- 5) Cartão + cupom com menos de R$ 10 no cartão (RN0035)
--    Cordas: total R$ 50,00. PROMO-0001 (R$ 45) + cartão R$ 5,00.
--    Sem cupom, esses R$ 5 seriam recusados pela RN0034.
--
-- 6) Cupons acima do valor da compra, com troco (RN0036)
--    Cordas: total R$ 50,00. TROCA-0002 (40) + TROCA-0003 (35) = R$ 75.
--    Emite um cupom TROCA-1000 de R$ 25,00 — o mesmo troco do exemplo
--    do DRS. Os três cupons juntos (95) são RECUSADOS: 95 − 20 = 75 já
--    cobriria os 50, logo o de R$ 20 seria desnecessário.
--
-- 7) Pedido gravado como EM_PROCESSAMENTO (RF0038)
--    Qualquer um dos anteriores; conferir o status na tela de pedidos.
--
-- Caso extra, para a RN0031: a interface Focusrite tem 2 em estoque.
-- Pedir 3 devolve 400.
-- ============================================================
