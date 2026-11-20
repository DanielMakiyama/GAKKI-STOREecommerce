-- Massa de dados do chatbot de recomendação (RNF0044).
-- Catálogo ampliado (28 instrumentos novos e as categorias Violões, Baixos e
-- Amplificadores), quatro clientes com perfis distintos, suas compras, suas
-- preferências e conversas com respostas avaliadas como úteis ou não úteis.
-- Os pedidos ficam fora de abr a set/2026, o gabarito dos testes da RF0055.
-- Senha dos clientes novos: Senha@2026.

INSERT INTO categoria (nome) VALUES ('Violões'), ('Baixos'), ('Amplificadores');

INSERT INTO fabricante (nome) VALUES
  ('Squier'),
  ('Ibanez'),
  ('Epiphone'),
  ('Gibson'),
  ('Takamine'),
  ('Ampeg'),
  ('Casio'),
  ('Roland'),
  ('Arturia'),
  ('Behringer'),
  ('Audio-Technica'),
  ('Santo Angelo'),
  ('Dunlop'),
  ('Snark'),
  ('D''Addario');

INSERT INTO instrumento (codigo, nome, descricao, fabricante_id, grupo_precificacao_id,
                         ano_fabricacao, valor_venda, quantidade_estoque, ativo)
SELECT v.codigo, v.nome, v.descricao, f.id, g.id, v.ano, v.preco, v.estoque, TRUE
  FROM (VALUES
  ('INST-0007', 'Guitarra Squier Affinity Stratocaster',
   'Guitarra de entrada com corpo leve em poplar, braço em maple e três captadores single-coil. Indicada para iniciantes, toca bem rock, blues e pop.',
   'Squier', 2025, 980.00, 6),
  ('INST-0008', 'Guitarra Ibanez RG421',
   'Braço Wizard III fino e rápido, dois humbuckers Quantum de saída alta e ponte fixa. Voltada para rock pesado e metal, para nível intermediário.',
   'Ibanez', 2025, 1820.00, 4),
  ('INST-0009', 'Guitarra Epiphone Les Paul Standard 60s',
   'Corpo em mogno com tampo em maple, dois humbuckers ProBucker e timbre encorpado. Clássica do rock e do blues, para nível intermediário.',
   'Epiphone', 2024, 2240.00, 3),
  ('INST-0010', 'Guitarra Gibson SG Standard',
   'Corpo em mogno, captadores 490R e 490T, acabamento nitrocelulose. Instrumento profissional para rock e hard rock, para nível avançado.',
   'Gibson', 2024, 5600.00, 2),
  ('INST-0011', 'Violão Yamaha C40',
   'Violão clássico de cordas de nylon, tampo em spruce. Som suave, ideal para iniciantes, MPB, bossa nova e música clássica.',
   'Yamaha', 2025, 560.00, 10),
  ('INST-0012', 'Violão Takamine GD11M-NS',
   'Violão folk de cordas de aço, tampo em mogno, corpo dreadnought. Som brilhante e projetado, comum no pop, folk e sertanejo.',
   'Takamine', 2025, 1540.00, 4),
  ('INST-0013', 'Baixo Squier Affinity Precision Bass PJ',
   'Baixo de 4 cordas com captadores Precision e Jazz Bass. Versátil e confortável, indicado para iniciantes e para rock, pop e funk.',
   'Squier', 2025, 1190.00, 5),
  ('INST-0014', 'Baixo Ibanez GSR200',
   'Baixo de 4 cordas com braço fino, captadores PJ e equalizador ativo de graves. Ótimo para slap e funk, para nível iniciante e intermediário.',
   'Ibanez', 2025, 1260.00, 4),
  ('INST-0015', 'Amplificador Boss Katana-50 MkII',
   'Amplificador de guitarra de 50 W com cinco tipos de timbre, efeitos embutidos e seletor de potência de 0,5 W para estudo em casa. Saída para fone.',
   'Boss', 2025, 1680.00, 4),
  ('INST-0016', 'Amplificador Fender Frontman 20G',
   'Amplificador de guitarra de 20 W com canal limpo e drive. Compacto, indicado para estudo em casa e para iniciantes.',
   'Fender', 2024, 630.00, 6),
  ('INST-0017', 'Amplificador Ampeg Rocket Bass RB-108',
   'Amplificador de baixo de 30 W com alto-falante de 8 polegadas e equalizador de 3 bandas. Para estudo e ensaios pequenos.',
   'Ampeg', 2025, 980.00, 4),
  ('INST-0018', 'Teclado Casio CT-S300',
   'Teclado de 61 teclas sensíveis ao toque, 400 timbres e conexão USB-MIDI. Leve e barato, indicado para iniciantes e para usar como controlador.',
   'Casio', 2025, 840.00, 7),
  ('INST-0019', 'Sintetizador Roland Juno-DS61',
   'Sintetizador de 61 teclas com mais de 1.000 timbres, sequenciador e entrada de microfone. Para palco e produção, nível intermediário.',
   'Roland', 2024, 4200.00, 2),
  ('INST-0020', 'Controlador MIDI Arturia MiniLab 3',
   'Controlador de 25 miniteclas, 8 pads e 8 knobs, conexão USB. Não produz som sozinho: controla programas de produção musical no computador.',
   'Arturia', 2025, 980.00, 5),
  ('INST-0021', 'Piano Digital Yamaha P-45',
   'Piano digital de 88 teclas com ação de martelo e som de piano de cauda. Indicado para estudar piano, do nível iniciante ao intermediário.',
   'Yamaha', 2025, 2940.00, 3),
  ('INST-0022', 'Pedal Boss BD-2 Blues Driver',
   'Overdrive de resposta dinâmica que imita um amplificador valvulado saturado. Referência no blues e no rock clássico.',
   'Boss', 2025, 770.00, 6),
  ('INST-0023', 'Pedal Ibanez TS9 Tube Screamer',
   'Overdrive clássico com médios realçados e ganho moderado. Muito usado no blues e antes de distorções para dar mais corpo.',
   'Ibanez', 2025, 840.00, 5),
  ('INST-0024', 'Pedal Boss DD-8 Digital Delay',
   'Delay com modos analógico, tape, reverse e até 10 segundos de repetição. Versátil para rock, blues e pop.',
   'Boss', 2025, 1120.00, 4),
  ('INST-0025', 'Interface Focusrite Scarlett Solo',
   'Interface de áudio USB com uma entrada de microfone com phantom 48 V e uma entrada de instrumento. Ideal para quem grava sozinho em casa.',
   'Focusrite', 2025, 840.00, 6),
  ('INST-0026', 'Interface Behringer UMC404HD',
   'Interface de áudio USB com quatro entradas de microfone ou instrumento e pré-amplificadores Midas. Para gravar vários instrumentos ao mesmo tempo.',
   'Behringer', 2024, 980.00, 4),
  ('INST-0027', 'Microfone Audio-Technica AT2020',
   'Microfone condensador cardioide para voz e violão em home studio. Usa cabo XLR e precisa de phantom power de 48 V.',
   'Audio-Technica', 2025, 700.00, 5),
  ('INST-0028', 'Fone de Ouvido Audio-Technica ATH-M50x',
   'Fone fechado de estúdio com resposta equilibrada, usado para gravação e mixagem. Cabo destacável.',
   'Audio-Technica', 2025, 1050.00, 5),
  ('INST-0029', 'Cabo P10 Santo Angelo 3 m',
   'Cabo de instrumento P10-P10 de 3 metros, para guitarra, baixo e teclado. Não serve para microfone XLR.',
   'Santo Angelo', 2026, 49.00, 30),
  ('INST-0030', 'Palhetas Dunlop Tortex (12 unidades)',
   'Pacote com 12 palhetas Tortex de espessura média, de boa pegada. Para guitarra e violão de aço.',
   'Dunlop', 2026, 42.00, 40),
  ('INST-0031', 'Afinador Snark ST-8',
   'Afinador de clipe cromático, funciona por vibração. Serve para guitarra, violão, baixo e ukulele.',
   'Snark', 2025, 84.00, 20),
  ('INST-0032', 'Cordas de Baixo D''Addario EXL170',
   'Encordoamento para baixo de 4 cordas, calibre .045-.100, aço niquelado. Timbre brilhante, bom para slap.',
   'D''Addario', 2026, 154.00, 15),
  ('INST-0033', 'Encordoamento Nylon D''Addario EJ45',
   'Encordoamento de nylon de tensão normal para violão clássico. Som quente, ideal para MPB e bossa nova.',
   'D''Addario', 2026, 70.00, 20),
  ('INST-0034', 'Capotraste Dunlop Trigger',
   'Capotraste de mola para violão e guitarra. Permite mudar o tom da música sem mudar o desenho dos acordes.',
   'Dunlop', 2025, 98.00, 15)
  ) AS v (codigo, nome, descricao, fabricante, ano, preco, estoque)
  JOIN fabricante f ON f.nome = v.fabricante
  JOIN grupo_precificacao g ON g.nome = 'Padrão 40%';

SELECT setval('seq_codigo_instrumento', GREATEST((SELECT last_value FROM seq_codigo_instrumento), 34));

INSERT INTO instrumento_categoria (instrumento_id, categoria_id)
SELECT i.id, c.id
  FROM (VALUES ('INST-0007', 'Guitarras'), ('INST-0008', 'Guitarras'), ('INST-0009', 'Guitarras'), ('INST-0010', 'Guitarras'), ('INST-0011', 'Violões'), ('INST-0012', 'Violões'), ('INST-0013', 'Baixos'), ('INST-0014', 'Baixos'), ('INST-0015', 'Amplificadores'), ('INST-0016', 'Amplificadores'), ('INST-0017', 'Amplificadores'), ('INST-0017', 'Baixos'), ('INST-0018', 'Teclados'), ('INST-0019', 'Teclados'), ('INST-0020', 'Teclados'), ('INST-0021', 'Teclados'), ('INST-0022', 'Pedais'), ('INST-0023', 'Pedais'), ('INST-0024', 'Pedais'), ('INST-0025', 'Interfaces de Áudio'), ('INST-0026', 'Interfaces de Áudio'), ('INST-0027', 'Acessórios'), ('INST-0028', 'Acessórios'), ('INST-0029', 'Acessórios'), ('INST-0030', 'Acessórios'), ('INST-0031', 'Acessórios'), ('INST-0032', 'Acessórios'), ('INST-0032', 'Baixos'), ('INST-0033', 'Acessórios'), ('INST-0033', 'Violões'), ('INST-0034', 'Acessórios'), ('INST-0034', 'Violões')) AS v (codigo, categoria)
  JOIN instrumento i ON i.codigo = v.codigo
  JOIN categoria   c ON c.nome   = v.categoria;

-- Entrada em estoque com custo = preço / 1,40 (margem do grupo Padrão 40%).
INSERT INTO item_estoque (instrumento_id, fornecedor_id, quantidade, valor_custo, data_entrada)
SELECT i.id, (SELECT id FROM fornecedor WHERE nome = 'Distribuidora Musical Brasil'), v.quantidade, v.custo, DATE '2026-09-01'
  FROM (VALUES ('INST-0007', 6, 700.00), ('INST-0008', 4, 1300.00), ('INST-0009', 3, 1600.00), ('INST-0010', 2, 4000.00), ('INST-0011', 10, 400.00), ('INST-0012', 4, 1100.00), ('INST-0013', 5, 850.00), ('INST-0014', 4, 900.00), ('INST-0015', 4, 1200.00), ('INST-0016', 6, 450.00), ('INST-0017', 4, 700.00), ('INST-0018', 7, 600.00), ('INST-0019', 2, 3000.00), ('INST-0020', 5, 700.00), ('INST-0021', 3, 2100.00), ('INST-0022', 6, 550.00), ('INST-0023', 5, 600.00), ('INST-0024', 4, 800.00), ('INST-0025', 6, 600.00), ('INST-0026', 4, 700.00), ('INST-0027', 5, 500.00), ('INST-0028', 5, 750.00), ('INST-0029', 30, 35.00), ('INST-0030', 40, 30.00), ('INST-0031', 20, 60.00), ('INST-0032', 15, 110.00), ('INST-0033', 20, 50.00), ('INST-0034', 15, 70.00)) AS v (codigo, quantidade, custo)
  JOIN instrumento i ON i.codigo = v.codigo;

INSERT INTO usuario (email, senha, papel, ativo) VALUES
  ('lucas.ferreira@example.com', '$2b$10$m66Kspniko4ebOfprGQ0GuEP0HxmE3w/9KNwDhOa11Z6oydAmLApK', 'CLIENTE', TRUE),
  ('carla.mendes@example.com', '$2b$10$m66Kspniko4ebOfprGQ0GuEP0HxmE3w/9KNwDhOa11Z6oydAmLApK', 'CLIENTE', TRUE),
  ('rafael.costa@example.com', '$2b$10$m66Kspniko4ebOfprGQ0GuEP0HxmE3w/9KNwDhOa11Z6oydAmLApK', 'CLIENTE', TRUE),
  ('ana.ribeiro@example.com', '$2b$10$m66Kspniko4ebOfprGQ0GuEP0HxmE3w/9KNwDhOa11Z6oydAmLApK', 'CLIENTE', TRUE);

-- O código sai da sequence: no banco de desenvolvimento os testes já cadastraram clientes.
INSERT INTO cliente (usuario_id, codigo, nome, cpf, genero, data_nascimento,
                     telefone_tipo, telefone_ddd, telefone_numero)
SELECT u.id, 'CLI-' || lpad(nextval('seq_codigo_cliente')::text, 4, '0'), v.nome, v.cpf, v.genero, v.nascimento,
       'CELULAR', v.ddd, v.telefone
  FROM (VALUES
  ('lucas.ferreira@example.com', 'Lucas Ferreira', '03582147002', 'MASCULINO', DATE '1997-05-14', '11', '991234567'),
  ('carla.mendes@example.com', 'Carla Mendes', '03693258180', 'FEMININO', DATE '2000-09-02', '11', '992345678'),
  ('rafael.costa@example.com', 'Rafael Costa', '03804369227', 'MASCULINO', DATE '1993-02-20', '19', '993456789'),
  ('ana.ribeiro@example.com', 'Ana Ribeiro', '03915480347', 'FEMININO', DATE '2004-12-08', '13', '994567890')
  ) AS v (email, nome, cpf, genero, nascimento, ddd, telefone)
  JOIN usuario u ON u.email = v.email
 ORDER BY v.email;

INSERT INTO endereco (cliente_id, apelido, tipo_residencia, tipo_logradouro, logradouro, numero,
                      bairro, cep, cidade, estado, pais, entrega, cobranca, principal)
SELECT cl.id, 'Casa', 'APARTAMENTO', v.tipo, v.logradouro, v.numero, v.bairro, v.cep, v.cidade, 'SP', 'Brasil', TRUE, TRUE, TRUE
  FROM (VALUES
  ('lucas.ferreira@example.com', 'RUA', 'Teodoro Sampaio', '820', 'Pinheiros', '05405000', 'São Paulo'),
  ('carla.mendes@example.com', 'RUA', 'Fradique Coutinho', '315', 'Vila Madalena', '05416010', 'São Paulo'),
  ('rafael.costa@example.com', 'AVENIDA', 'Norte-Sul', '1200', 'Cambuí', '13025320', 'Campinas'),
  ('ana.ribeiro@example.com', 'RUA', 'Bahia', '77', 'Gonzaga', '11065430', 'Santos')
  ) AS v (email, tipo, logradouro, numero, bairro, cep, cidade)
  JOIN usuario u  ON u.email = v.email
  JOIN cliente cl ON cl.usuario_id = u.id;

INSERT INTO cartao (cliente_id, bandeira_id, apelido, ultimos_digitos, nome_titular, validade_mes, validade_ano, preferencial)
SELECT cl.id, b.id, 'Cartão principal', v.digitos, v.titular, 10, 2030, TRUE
  FROM (VALUES
  ('lucas.ferreira@example.com', 'Mastercard', '2468', 'LUCAS FERREIRA'),
  ('carla.mendes@example.com', 'Visa', '1357', 'CARLA MENDES'),
  ('rafael.costa@example.com', 'Elo', '8642', 'RAFAEL COSTA'),
  ('ana.ribeiro@example.com', 'Visa', '9753', 'ANA RIBEIRO')
  ) AS v (email, bandeira, digitos, titular)
  JOIN usuario  u  ON u.email = v.email
  JOIN cliente  cl ON cl.usuario_id = u.id
  JOIN bandeira b  ON b.nome = v.bandeira;

INSERT INTO pedido (numero, cliente_id, endereco_entrega_id, status, valor_frete, valor_total, criado_em)
SELECT v.numero, cl.id, e.id, 'ENTREGUE', v.frete, v.total, v.criado_em
  FROM (VALUES
  ('PED-92001', 'lucas.ferreira@example.com', 25.00, 1075.00, '2025-05-10 15:20:00-03'::timestamptz),
  ('PED-92002', 'lucas.ferreira@example.com', 20.00, 699.00, '2025-08-22 19:05:00-03'::timestamptz),
  ('PED-92003', 'lucas.ferreira@example.com', 20.00, 762.00, '2025-11-28 10:40:00-03'::timestamptz),
  ('PED-92004', 'lucas.ferreira@example.com', 20.00, 825.00, '2026-02-14 16:15:00-03'::timestamptz),
  ('PED-92005', 'carla.mendes@example.com', 15.00, 995.00, '2025-06-03 11:30:00-03'::timestamptz),
  ('PED-92006', 'carla.mendes@example.com', 15.00, 855.00, '2025-09-17 20:10:00-03'::timestamptz),
  ('PED-92007', 'carla.mendes@example.com', 15.00, 1065.00, '2026-01-08 14:45:00-03'::timestamptz),
  ('PED-92008', 'rafael.costa@example.com', 20.00, 1259.00, '2025-08-05 18:00:00-03'::timestamptz),
  ('PED-92009', 'rafael.costa@example.com', 15.00, 995.00, '2025-11-29 13:25:00-03'::timestamptz),
  ('PED-92010', 'rafael.costa@example.com', 15.00, 169.00, '2026-03-12 17:50:00-03'::timestamptz),
  ('PED-92011', 'ana.ribeiro@example.com', 20.00, 664.00, '2026-01-20 09:15:00-03'::timestamptz),
  ('PED-92012', 'ana.ribeiro@example.com', 15.00, 85.00, '2026-03-02 19:40:00-03'::timestamptz)
  ) AS v (numero, email, frete, total, criado_em)
  JOIN usuario  u  ON u.email = v.email
  JOIN cliente  cl ON cl.usuario_id = u.id
  JOIN endereco e  ON e.cliente_id = cl.id AND e.principal;

INSERT INTO item_pedido (pedido_id, instrumento_id, instrumento_nome, quantidade, valor_unitario)
SELECT p.id, i.id, i.nome, v.quantidade, v.valor_unitario
  FROM (VALUES
  ('PED-92001', 'INST-0007', 1, 980.00),
  ('PED-92001', 'INST-0005', 2, 35.00),
  ('PED-92002', 'INST-0016', 1, 630.00),
  ('PED-92002', 'INST-0029', 1, 49.00),
  ('PED-92003', 'INST-0003', 1, 700.00),
  ('PED-92003', 'INST-0030', 1, 42.00),
  ('PED-92004', 'INST-0022', 1, 770.00),
  ('PED-92004', 'INST-0005', 1, 35.00),
  ('PED-92005', 'INST-0020', 1, 980.00),
  ('PED-92006', 'INST-0025', 1, 840.00),
  ('PED-92007', 'INST-0028', 1, 1050.00),
  ('PED-92008', 'INST-0013', 1, 1190.00),
  ('PED-92008', 'INST-0029', 1, 49.00),
  ('PED-92009', 'INST-0017', 1, 980.00),
  ('PED-92010', 'INST-0032', 1, 154.00),
  ('PED-92011', 'INST-0011', 1, 560.00),
  ('PED-92011', 'INST-0031', 1, 84.00),
  ('PED-92012', 'INST-0033', 1, 70.00)
  ) AS v (numero, codigo, quantidade, valor_unitario)
  JOIN pedido      p ON p.numero = v.numero
  JOIN instrumento i ON i.codigo = v.codigo;

INSERT INTO pagamento (pedido_id, cartao_id, valor)
SELECT p.id, ca.id, p.valor_total
  FROM pedido p
  JOIN cartao ca ON ca.cliente_id = p.cliente_id AND ca.preferencial
 WHERE p.numero BETWEEN 'PED-92001' AND 'PED-92012';

INSERT INTO preferencia_cliente (cliente_id, nivel, orcamento_maximo, observacoes, criado_em)
SELECT cl.id, v.nivel, v.orcamento, v.observacoes, TIMESTAMPTZ '2026-10-01 10:00:00-03'
  FROM (VALUES
  ('lucas.ferreira@example.com', 'INTERMEDIARIO', 2500.00, 'Toco em uma banda de blues rock aos fins de semana.'),
  ('carla.mendes@example.com', 'INICIANTE', 3000.00, 'Produzo música eletrônica em casa e quero começar a gravar voz.'),
  ('rafael.costa@example.com', 'INTERMEDIARIO', 2000.00, 'Toco baixo com slap em uma banda de funk.'),
  ('ana.ribeiro@example.com', 'INICIANTE', 1000.00, 'Quero aprender a tocar bossa nova no violão.')
  ) AS v (email, nivel, orcamento, observacoes)
  JOIN usuario u  ON u.email = v.email
  JOIN cliente cl ON cl.usuario_id = u.id;

INSERT INTO preferencia_cliente_categoria (preferencia_id, categoria_id)
SELECT pc.id, c.id
  FROM (VALUES ('lucas.ferreira@example.com', 'Guitarras'), ('lucas.ferreira@example.com', 'Pedais'), ('lucas.ferreira@example.com', 'Amplificadores'), ('carla.mendes@example.com', 'Teclados'), ('carla.mendes@example.com', 'Interfaces de Áudio'), ('carla.mendes@example.com', 'Acessórios'), ('rafael.costa@example.com', 'Baixos'), ('rafael.costa@example.com', 'Amplificadores'), ('ana.ribeiro@example.com', 'Violões'), ('ana.ribeiro@example.com', 'Acessórios')) AS v (email, categoria)
  JOIN usuario u  ON u.email = v.email
  JOIN cliente cl ON cl.usuario_id = u.id
  JOIN preferencia_cliente pc ON pc.cliente_id = cl.id
  JOIN categoria c ON c.nome = v.categoria;

INSERT INTO preferencia_cliente_estilo (preferencia_id, estilo)
SELECT pc.id, v.estilo
  FROM (VALUES ('lucas.ferreira@example.com', 'ROCK'), ('lucas.ferreira@example.com', 'BLUES'), ('carla.mendes@example.com', 'ELETRONICA'), ('carla.mendes@example.com', 'POP'), ('rafael.costa@example.com', 'FUNK'), ('rafael.costa@example.com', 'ROCK'), ('ana.ribeiro@example.com', 'MPB')) AS v (email, estilo)
  JOIN usuario u  ON u.email = v.email
  JOIN cliente cl ON cl.usuario_id = u.id
  JOIN preferencia_cliente pc ON pc.cliente_id = cl.id;

INSERT INTO conversa_chat (cliente_id, iniciada_em)
SELECT cl.id, v.iniciada_em
  FROM (VALUES
  ('lucas.ferreira@example.com', '2026-10-05 19:10:00-03'::timestamptz),
  ('lucas.ferreira@example.com', '2026-10-18 21:00:00-03'::timestamptz),
  ('lucas.ferreira@example.com', '2026-11-02 18:40:00-03'::timestamptz),
  ('lucas.ferreira@example.com', '2026-11-10 20:15:00-03'::timestamptz),
  ('carla.mendes@example.com', '2026-10-07 14:20:00-03'::timestamptz),
  ('carla.mendes@example.com', '2026-10-20 16:00:00-03'::timestamptz),
  ('carla.mendes@example.com', '2026-11-05 10:30:00-03'::timestamptz),
  ('rafael.costa@example.com', '2026-10-09 20:00:00-03'::timestamptz),
  ('rafael.costa@example.com', '2026-10-25 19:30:00-03'::timestamptz),
  ('rafael.costa@example.com', '2026-11-07 18:00:00-03'::timestamptz),
  ('ana.ribeiro@example.com', '2026-10-12 15:00:00-03'::timestamptz),
  ('ana.ribeiro@example.com', '2026-11-03 17:45:00-03'::timestamptz),
  ('maria.souza@example.com', '2026-10-14 11:00:00-03'::timestamptz),
  ('joao.pereira@example.com', '2026-10-22 22:10:00-03'::timestamptz)
  ) AS v (email, iniciada_em)
  JOIN usuario u  ON u.email = v.email
  JOIN cliente cl ON cl.usuario_id = u.id;

-- Mensagens anteriores ao primeiro ajuste fino: modelo_ia_id fica nulo.
CREATE TEMP TABLE semente_mensagem (email TEXT, iniciada_em TIMESTAMPTZ, criado_em TIMESTAMPTZ,
                                    autor TEXT, texto TEXT, avaliacao TEXT) ON COMMIT DROP;
INSERT INTO semente_mensagem VALUES
  ('lucas.ferreira@example.com', '2026-10-05 19:10:00-03'::timestamptz, '2026-10-05T19:10:00-03:00'::timestamptz, 'CLIENTE', 'Oi! Quero melhorar meu timbre de blues. O que vocês recomendam?', NULL),
  ('lucas.ferreira@example.com', '2026-10-05 19:10:00-03'::timestamptz, '2026-10-05T19:11:00-03:00'::timestamptz, 'ASSISTENTE', 'Pelo seu histórico, você já tem a Squier Affinity Stratocaster, o amplificador Fender Frontman 20G e os pedais Boss DS-1 e BD-2. Para blues, o que mais muda o timbre agora é o amplificador: o Boss Katana-50 MkII (R$ 1.680,00) tem 50 W, canal de crunch e efeitos embutidos, e combina com o BD-2 que você já usa. Se preferir um overdrive com médios mais marcados, o Ibanez TS9 Tube Screamer (R$ 840,00) é um clássico do blues. Os dois cabem no seu orçamento de até R$ 2.500,00.', 'UTIL'),
  ('lucas.ferreira@example.com', '2026-10-05 19:10:00-03'::timestamptz, '2026-10-05T19:12:00-03:00'::timestamptz, 'CLIENTE', 'O Katana serve para tocar em casa também?', NULL),
  ('lucas.ferreira@example.com', '2026-10-05 19:10:00-03'::timestamptz, '2026-10-05T19:13:00-03:00'::timestamptz, 'ASSISTENTE', 'Serve, sim. O Katana-50 MkII tem um seletor que reduz a potência para 0,5 W, o que permite tocar em volume baixo sem perder o timbre, e tem saída para fone de ouvido. Ele funciona tanto para estudar em casa quanto para os ensaios da sua banda.', 'UTIL'),
  ('lucas.ferreira@example.com', '2026-10-18 21:00:00-03'::timestamptz, '2026-10-18T21:00:00-03:00'::timestamptz, 'CLIENTE', 'Tem alguma guitarra com humbucker até 2.500?', NULL),
  ('lucas.ferreira@example.com', '2026-10-18 21:00:00-03'::timestamptz, '2026-10-18T21:01:00-03:00'::timestamptz, 'ASSISTENTE', 'Temos duas opções no seu orçamento: a Epiphone Les Paul Standard 60s (R$ 2.240,00), com dois humbuckers ProBucker e timbre encorpado, ótima para rock e blues; e a Ibanez RG421 (R$ 1.820,00), com braço fino e captadores de saída alta, mais voltada para rock pesado e metal. Pelo seu gosto por blues rock, a Les Paul combina mais.', 'UTIL'),
  ('lucas.ferreira@example.com', '2026-10-18 21:00:00-03'::timestamptz, '2026-10-18T21:02:00-03:00'::timestamptz, 'CLIENTE', 'E a Telecaster Vintage II?', NULL),
  ('lucas.ferreira@example.com', '2026-10-18 21:00:00-03'::timestamptz, '2026-10-18T21:03:00-03:00'::timestamptz, 'ASSISTENTE', 'A Fender Telecaster Vintage II é uma ótima escolha! Pode adicionar ao carrinho.', 'NAO_UTIL'),
  ('lucas.ferreira@example.com', '2026-10-18 21:00:00-03'::timestamptz, '2026-10-18T21:04:00-03:00'::timestamptz, 'CLIENTE', 'Ela aparece como esgotada aqui.', NULL),
  ('lucas.ferreira@example.com', '2026-10-18 21:00:00-03'::timestamptz, '2026-10-18T21:05:00-03:00'::timestamptz, 'ASSISTENTE', 'Você tem razão, me desculpe. A Fender Telecaster Vintage II está esgotada e sem previsão de reposição. Entre as guitarras disponíveis no seu orçamento, a Epiphone Les Paul Standard 60s (R$ 2.240,00) é a melhor opção para blues rock.', 'UTIL'),
  ('lucas.ferreira@example.com', '2026-11-02 18:40:00-03'::timestamptz, '2026-11-02T18:40:00-03:00'::timestamptz, 'CLIENTE', 'Preciso de cordas de novo', NULL),
  ('lucas.ferreira@example.com', '2026-11-02 18:40:00-03'::timestamptz, '2026-11-02T18:41:00-03:00'::timestamptz, 'ASSISTENTE', 'Você já comprou o Jogo de Cordas Elixir Nanoweb .010 (R$ 35,00), que tem um revestimento que dura mais que o de cordas comuns. Para a sua Stratocaster, ele continua sendo a melhor opção. Se quiser, leve também as Palhetas Dunlop Tortex (R$ 42,00, com 12 unidades), que você já usou antes.', 'UTIL'),
  ('lucas.ferreira@example.com', '2026-11-02 18:40:00-03'::timestamptz, '2026-11-02T18:42:00-03:00'::timestamptz, 'CLIENTE', 'Qual a diferença entre o DS-1 e o TS9?', NULL),
  ('lucas.ferreira@example.com', '2026-11-02 18:40:00-03'::timestamptz, '2026-11-02T18:43:00-03:00'::timestamptz, 'ASSISTENTE', 'O Boss DS-1 é uma distorção: tem mais ganho e um som mais agressivo, bom para rock. O Ibanez TS9 Tube Screamer é um overdrive: tem menos ganho, realça os médios e soa como um amplificador valvulado levemente saturado, o som típico do blues. Muitos guitarristas usam o TS9 antes do DS-1 para ter as duas sonoridades.', 'UTIL'),
  ('lucas.ferreira@example.com', '2026-11-10 20:15:00-03'::timestamptz, '2026-11-10T20:15:00-03:00'::timestamptz, 'CLIENTE', 'Me indica um delay', NULL),
  ('lucas.ferreira@example.com', '2026-11-10 20:15:00-03'::timestamptz, '2026-11-10T20:16:00-03:00'::timestamptz, 'ASSISTENTE', 'Temos vários pedais no catálogo. Dê uma olhada na categoria Pedais.', 'NAO_UTIL'),
  ('lucas.ferreira@example.com', '2026-11-10 20:15:00-03'::timestamptz, '2026-11-10T20:17:00-03:00'::timestamptz, 'CLIENTE', 'Mas qual você recomenda?', NULL),
  ('lucas.ferreira@example.com', '2026-11-10 20:15:00-03'::timestamptz, '2026-11-10T20:18:00-03:00'::timestamptz, 'ASSISTENTE', 'Recomendo o Boss DD-8 Digital Delay (R$ 1.120,00). Ele tem modos analógico, tape e reverse, com até 10 segundos de repetição. Para blues e rock, o modo analógico dá repetições mais quentes, que combinam com o BD-2 que você já tem.', 'UTIL'),
  ('carla.mendes@example.com', '2026-10-07 14:20:00-03'::timestamptz, '2026-10-07T14:20:00-03:00'::timestamptz, 'CLIENTE', 'Oi, comecei a produzir música eletrônica em casa. O que falta no meu setup?', NULL),
  ('carla.mendes@example.com', '2026-10-07 14:20:00-03'::timestamptz, '2026-10-07T14:21:00-03:00'::timestamptz, 'ASSISTENTE', 'Pelo seu histórico, você já tem o controlador MIDI Arturia MiniLab 3, a interface Focusrite Scarlett Solo e o fone Audio-Technica ATH-M50x, que formam o básico de um home studio. Para começar a gravar voz, o próximo item é um microfone condensador: o Audio-Technica AT2020 (R$ 700,00) funciona direto na sua Scarlett Solo, que tem phantom power de 48 V.', 'UTIL'),
  ('carla.mendes@example.com', '2026-10-07 14:20:00-03'::timestamptz, '2026-10-07T14:22:00-03:00'::timestamptz, 'CLIENTE', 'Quero também um teclado de verdade, com mais teclas.', NULL),
  ('carla.mendes@example.com', '2026-10-07 14:20:00-03'::timestamptz, '2026-10-07T14:23:00-03:00'::timestamptz, 'ASSISTENTE', 'O Roland Juno-DS61 (R$ 4.200,00) é um sintetizador completo para produção, mas passa do seu orçamento de R$ 3.000,00. Dentro do orçamento, o Yamaha P-45 (R$ 2.940,00) tem 88 teclas com ação de martelo, ideal se você quer estudar piano, e o Casio CT-S300 (R$ 840,00) tem 61 teclas sensíveis ao toque e também funciona como controlador USB. Como você está começando, o CT-S300 tem o melhor custo-benefício.', 'UTIL'),
  ('carla.mendes@example.com', '2026-10-20 16:00:00-03'::timestamptz, '2026-10-20T16:00:00-03:00'::timestamptz, 'CLIENTE', 'A Scarlett Solo dá conta de gravar dois microfones?', NULL),
  ('carla.mendes@example.com', '2026-10-20 16:00:00-03'::timestamptz, '2026-10-20T16:01:00-03:00'::timestamptz, 'ASSISTENTE', 'Não. A Scarlett Solo tem só uma entrada de microfone. Para gravar dois microfones ao mesmo tempo, você precisa de uma interface com mais entradas: a Focusrite Scarlett 2i2 (R$ 1.260,00) tem duas, e a Behringer UMC404HD (R$ 980,00) tem quatro.', 'UTIL'),
  ('carla.mendes@example.com', '2026-10-20 16:00:00-03'::timestamptz, '2026-10-20T16:02:00-03:00'::timestamptz, 'CLIENTE', 'Qual guitarra você me indica?', NULL),
  ('carla.mendes@example.com', '2026-10-20 16:00:00-03'::timestamptz, '2026-10-20T16:03:00-03:00'::timestamptz, 'ASSISTENTE', 'A Gibson SG Standard (R$ 5.600,00) é uma guitarra profissional, excelente para rock.', 'NAO_UTIL'),
  ('carla.mendes@example.com', '2026-10-20 16:00:00-03'::timestamptz, '2026-10-20T16:04:00-03:00'::timestamptz, 'CLIENTE', 'Sou iniciante e ainda não toco guitarra, queria algo simples.', NULL),
  ('carla.mendes@example.com', '2026-10-20 16:00:00-03'::timestamptz, '2026-10-20T16:05:00-03:00'::timestamptz, 'ASSISTENTE', 'Entendi. Para quem está começando, a Squier Affinity Stratocaster (R$ 980,00) é a mais indicada: é leve, confortável e versátil. Como você produz em casa, dá para ligá-la direto na entrada de instrumento da sua Scarlett Solo e gravar sem amplificador.', 'UTIL'),
  ('carla.mendes@example.com', '2026-11-05 10:30:00-03'::timestamptz, '2026-11-05T10:30:00-03:00'::timestamptz, 'CLIENTE', 'Fone ou caixa de som para mixar?', NULL),
  ('carla.mendes@example.com', '2026-11-05 10:30:00-03'::timestamptz, '2026-11-05T10:31:00-03:00'::timestamptz, 'ASSISTENTE', 'O ideal é ter os dois, mas o seu ATH-M50x já resolve boa parte do trabalho: ele tem resposta equilibrada e é usado em estúdios. No momento não temos monitores de referência no catálogo, então recomendo mixar no fone e conferir o resultado em outras caixas, como as do celular ou do carro.', 'UTIL'),
  ('carla.mendes@example.com', '2026-11-05 10:30:00-03'::timestamptz, '2026-11-05T10:32:00-03:00'::timestamptz, 'CLIENTE', 'Preciso de algum cabo para o microfone?', NULL),
  ('carla.mendes@example.com', '2026-11-05 10:30:00-03'::timestamptz, '2026-11-05T10:33:00-03:00'::timestamptz, 'ASSISTENTE', 'Sim, o Cabo P10 Santo Angelo 3 m (R$ 49,00) serve para o microfone.', 'NAO_UTIL'),
  ('carla.mendes@example.com', '2026-11-05 10:30:00-03'::timestamptz, '2026-11-05T10:34:00-03:00'::timestamptz, 'CLIENTE', 'Mas o AT2020 não usa XLR?', NULL),
  ('carla.mendes@example.com', '2026-11-05 10:30:00-03'::timestamptz, '2026-11-05T10:35:00-03:00'::timestamptz, 'ASSISTENTE', 'Você está certa, me desculpe pelo erro. O AT2020 usa cabo XLR, e o Cabo P10 Santo Angelo é para instrumentos, como guitarra e baixo. No momento não temos cabo XLR no catálogo.', 'UTIL'),
  ('rafael.costa@example.com', '2026-10-09 20:00:00-03'::timestamptz, '2026-10-09T20:00:00-03:00'::timestamptz, 'CLIENTE', 'Estou pensando em trocar meu baixo.', NULL),
  ('rafael.costa@example.com', '2026-10-09 20:00:00-03'::timestamptz, '2026-10-09T20:01:00-03:00'::timestamptz, 'ASSISTENTE', 'Hoje você tem o Squier Affinity Precision Bass PJ. Dentro do seu orçamento de R$ 2.000,00, o Ibanez GSR200 (R$ 1.260,00) tem braço mais fino e equalizador ativo de graves, ótimo para o slap do funk. Ele soa mais moderno que o Precision, que tem um timbre mais clássico.', 'UTIL'),
  ('rafael.costa@example.com', '2026-10-09 20:00:00-03'::timestamptz, '2026-10-09T20:02:00-03:00'::timestamptz, 'CLIENTE', 'Ele funciona com o meu amplificador?', NULL),
  ('rafael.costa@example.com', '2026-10-09 20:00:00-03'::timestamptz, '2026-10-09T20:03:00-03:00'::timestamptz, 'ASSISTENTE', 'Funciona. O Ampeg Rocket Bass RB-108 que você comprou é um amplificador de baixo e aceita qualquer baixo elétrico pela entrada P10. Com o GSR200, use o equalizador do próprio baixo para dar mais presença ao slap.', 'UTIL'),
  ('rafael.costa@example.com', '2026-10-25 19:30:00-03'::timestamptz, '2026-10-25T19:30:00-03:00'::timestamptz, 'CLIENTE', 'Qual pedal vocês têm para baixo?', NULL),
  ('rafael.costa@example.com', '2026-10-25 19:30:00-03'::timestamptz, '2026-10-25T19:31:00-03:00'::timestamptz, 'ASSISTENTE', 'O Boss DS-1 Distortion (R$ 700,00) é um dos pedais mais vendidos da loja.', 'NAO_UTIL'),
  ('rafael.costa@example.com', '2026-10-25 19:30:00-03'::timestamptz, '2026-10-25T19:32:00-03:00'::timestamptz, 'CLIENTE', 'Distorção para baixo? Esse não é para guitarra?', NULL),
  ('rafael.costa@example.com', '2026-10-25 19:30:00-03'::timestamptz, '2026-10-25T19:33:00-03:00'::timestamptz, 'ASSISTENTE', 'Você tem razão: o DS-1 foi feito para guitarra e corta muitos graves no baixo. No momento não temos pedais específicos para baixo no catálogo. Para seu estilo, investir no Ibanez GSR200 (R$ 1.260,00), com equalizador ativo, muda mais o timbre do que um pedal de guitarra.', 'UTIL'),
  ('rafael.costa@example.com', '2026-11-07 18:00:00-03'::timestamptz, '2026-11-07T18:00:00-03:00'::timestamptz, 'CLIENTE', 'Quando devo trocar as cordas do baixo?', NULL),
  ('rafael.costa@example.com', '2026-11-07 18:00:00-03'::timestamptz, '2026-11-07T18:01:00-03:00'::timestamptz, 'ASSISTENTE', 'Depende do quanto você toca, mas uma referência comum é trocar a cada 3 a 6 meses, ou quando o som ficar abafado e sem brilho. Você comprou as Cordas de Baixo D''Addario EXL170 (R$ 154,00) em março; se toca com frequência, já está na hora. Limpar as cordas depois de tocar ajuda a durarem mais.', 'UTIL'),
  ('ana.ribeiro@example.com', '2026-10-12 15:00:00-03'::timestamptz, '2026-10-12T15:00:00-03:00'::timestamptz, 'CLIENTE', 'Comprei meu primeiro violão. O que mais preciso?', NULL),
  ('ana.ribeiro@example.com', '2026-10-12 15:00:00-03'::timestamptz, '2026-10-12T15:01:00-03:00'::timestamptz, 'ASSISTENTE', 'Parabéns pelo Violão Yamaha C40! Você já tem o Afinador Snark ST-8 e o Encordoamento Nylon D''Addario EJ45. Para a bossa nova, um item que ajuda muito é o Capotraste Dunlop Trigger (R$ 98,00): ele permite tocar a mesma música em outro tom sem mudar os acordes, o que facilita acompanhar a voz.', 'UTIL'),
  ('ana.ribeiro@example.com', '2026-10-12 15:00:00-03'::timestamptz, '2026-10-12T15:02:00-03:00'::timestamptz, 'CLIENTE', 'Vale a pena um violão de aço?', NULL),
  ('ana.ribeiro@example.com', '2026-10-12 15:00:00-03'::timestamptz, '2026-10-12T15:03:00-03:00'::timestamptz, 'ASSISTENTE', 'Depende do estilo. O violão de nylon, como o seu C40, tem som mais suave e é o mais usado na MPB e na bossa nova. O de aço, como o Takamine GD11M-NS (R$ 1.540,00), tem som mais brilhante, comum no pop e no folk, mas as cordas são mais duras para quem está começando e ele passa do seu orçamento. Para o seu caso, eu continuaria no nylon por enquanto.', 'UTIL'),
  ('ana.ribeiro@example.com', '2026-11-03 17:45:00-03'::timestamptz, '2026-11-03T17:45:00-03:00'::timestamptz, 'CLIENTE', 'Meu violão fica desafinado rápido', NULL),
  ('ana.ribeiro@example.com', '2026-11-03 17:45:00-03'::timestamptz, '2026-11-03T17:46:00-03:00'::timestamptz, 'ASSISTENTE', 'Recomendo trocar por um violão melhor, como o Takamine GD11M-NS (R$ 1.540,00).', 'NAO_UTIL'),
  ('ana.ribeiro@example.com', '2026-11-03 17:45:00-03'::timestamptz, '2026-11-03T17:47:00-03:00'::timestamptz, 'CLIENTE', 'Não quero trocar de violão, quero resolver o problema.', NULL),
  ('ana.ribeiro@example.com', '2026-11-03 17:45:00-03'::timestamptz, '2026-11-03T17:48:00-03:00'::timestamptz, 'ASSISTENTE', 'Claro. Cordas de nylon novas esticam bastante nos primeiros dias, então é normal desafinar até elas assentarem. Afine com o seu Snark ST-8 sempre antes de tocar e evite deixar o violão perto de janelas ou do ar-condicionado, porque a variação de temperatura também desafina. Se continuar depois de uma semana, vale trocar o Encordoamento Nylon D''Addario EJ45 (R$ 70,00).', 'UTIL'),
  ('maria.souza@example.com', '2026-10-14 11:00:00-03'::timestamptz, '2026-10-14T11:00:00-03:00'::timestamptz, 'CLIENTE', 'Como funciona a troca de um produto?', NULL),
  ('maria.souza@example.com', '2026-10-14 11:00:00-03'::timestamptz, '2026-10-14T11:01:00-03:00'::timestamptz, 'ASSISTENTE', 'Depois que o pedido é entregue, você pode pedir a troca na tela Meus Pedidos, escolhendo os itens. A loja autoriza a troca e, quando o produto volta, você recebe um cupom de troca com o valor dele, que pode ser usado em qualquer compra.', 'UTIL'),
  ('maria.souza@example.com', '2026-10-14 11:00:00-03'::timestamptz, '2026-10-14T11:02:00-03:00'::timestamptz, 'CLIENTE', 'O frete é grátis?', NULL),
  ('maria.souza@example.com', '2026-10-14 11:00:00-03'::timestamptz, '2026-10-14T11:03:00-03:00'::timestamptz, 'ASSISTENTE', 'Não. O frete é de R$ 15,00 para a primeira peça, mais R$ 5,00 por peça adicional, com um adicional de R$ 10,00 para entregas fora de São Paulo.', 'UTIL'),
  ('joao.pereira@example.com', '2026-10-22 22:10:00-03'::timestamptz, '2026-10-22T22:10:00-03:00'::timestamptz, 'CLIENTE', 'Dá para pagar com dois cartões?', NULL),
  ('joao.pereira@example.com', '2026-10-22 22:10:00-03'::timestamptz, '2026-10-22T22:11:00-03:00'::timestamptz, 'ASSISTENTE', 'Dá, sim. No checkout você pode dividir o valor entre mais de um cartão, desde que cada um pague pelo menos R$ 10,00. Também dá para usar cupons de troca junto com os cartões.', 'UTIL'),
  ('joao.pereira@example.com', '2026-10-22 22:10:00-03'::timestamptz, '2026-10-22T22:12:00-03:00'::timestamptz, 'CLIENTE', 'Me recomenda alguma coisa?', NULL),
  ('joao.pereira@example.com', '2026-10-22 22:10:00-03'::timestamptz, '2026-10-22T22:13:00-03:00'::timestamptz, 'ASSISTENTE', 'Recomendo a Guitarra Gibson SG Standard (R$ 5.600,00).', 'NAO_UTIL'),
  ('joao.pereira@example.com', '2026-10-22 22:10:00-03'::timestamptz, '2026-10-22T22:14:00-03:00'::timestamptz, 'CLIENTE', 'Por que essa?', NULL),
  ('joao.pereira@example.com', '2026-10-22 22:10:00-03'::timestamptz, '2026-10-22T22:15:00-03:00'::timestamptz, 'ASSISTENTE', 'Desculpe, foi uma sugestão sem base no seu perfil. Pelo seu histórico, você compra pedais, teclados e interfaces de áudio, então o controlador Arturia MiniLab 3 (R$ 980,00) ou a interface Behringer UMC404HD (R$ 980,00), com quatro entradas, combinam mais com o que você usa. Se preencher suas preferências no perfil, consigo indicar opções mais certeiras.', 'UTIL');

INSERT INTO mensagem_chat (conversa_id, autor, texto, criado_em)
SELECT cc.id, s.autor, s.texto, s.criado_em
  FROM semente_mensagem s
  JOIN usuario u        ON u.email = s.email
  JOIN cliente cl       ON cl.usuario_id = u.id
  JOIN conversa_chat cc ON cc.cliente_id = cl.id AND cc.iniciada_em = s.iniciada_em;

INSERT INTO feedback_resposta (mensagem_id, cliente_id, avaliacao, criado_em)
SELECT m.id, cl.id, s.avaliacao, s.criado_em + INTERVAL '30 seconds'
  FROM semente_mensagem s
  JOIN usuario u        ON u.email = s.email
  JOIN cliente cl       ON cl.usuario_id = u.id
  JOIN conversa_chat cc ON cc.cliente_id = cl.id AND cc.iniciada_em = s.iniciada_em
  JOIN mensagem_chat m  ON m.conversa_id = cc.id AND m.criado_em = s.criado_em
 WHERE s.avaliacao IS NOT NULL;

-- Falha se algum JOIN acima descartou linhas em silêncio.
DO $$
BEGIN
    IF (SELECT count(*) FROM instrumento WHERE codigo BETWEEN 'INST-0007' AND 'INST-0034') <> 28 THEN
        RAISE EXCEPTION 'V1002: esperados 28 instrumentos novos';
    END IF;
    IF (SELECT count(*) FROM instrumento_categoria ic JOIN instrumento i ON i.id = ic.instrumento_id
         WHERE i.codigo BETWEEN 'INST-0007' AND 'INST-0034') <> 32 THEN
        RAISE EXCEPTION 'V1002: esperados 32 vínculos de categoria';
    END IF;
    IF (SELECT count(*) FROM pedido WHERE numero BETWEEN 'PED-92001' AND 'PED-92012') <> 12
       OR (SELECT count(*) FROM pagamento pg JOIN pedido p ON p.id = pg.pedido_id
            WHERE p.numero BETWEEN 'PED-92001' AND 'PED-92012') <> 12 THEN
        RAISE EXCEPTION 'V1002: esperados 12 pedidos, cada um com pagamento';
    END IF;
    IF (SELECT count(*) FROM preferencia_cliente) <> 4 THEN
        RAISE EXCEPTION 'V1002: esperadas 4 preferências';
    END IF;
    IF (SELECT count(*) FROM mensagem_chat) <> 62 OR (SELECT count(*) FROM feedback_resposta) <> 31 THEN
        RAISE EXCEPTION 'V1002: esperadas 62 mensagens e 31 avaliações';
    END IF;
END $$;
