-- ============================================================
-- Massa de dados da análise de vendas (RF0055, RNF0043)
--
-- O enunciado da atividade pede vendas de várias categorias, distribuídas
-- em meses diferentes e com compras em status distintos, ENTREGUE e
-- outros. Esta migration monta esse histórico de abril a setembro de 2026.
--
-- Numerada como V1000 para rodar depois do V999: ela usa os clientes, os
-- endereços, os cartões e os instrumentos que o seed principal cria.
--
-- POR QUE OS PEDIDOS SÃO DE MARIA (CLI-0002) E JOÃO (CLI-0003)
-- A suíte E2E inteira usa o cliente de demonstração (CLI-0001). Deixar o
-- histórico fora dele mantém a tela "Meus pedidos" desse cliente e as
-- trocas da suíte iguais ao que eram antes desta migration.
--
-- POR QUE ABRIL A SETEMBRO
-- Os pedidos que a suíte E2E cria nascem com a data de hoje. Um período
-- fechado em setembro fica imune a eles, e os valores abaixo podem ser
-- conferidos à mão em qualquer execução. Os cenários que precisam de uma
-- compra nova (a que passa a ENTREGUE, por exemplo) usam o mês corrente.
--
-- POR QUE NENHUM PEDIDO EM_PROCESSAMENTO
-- Pedido EM_PROCESSAMENTO reserva estoque (RN0044, quantidadeComStatus
-- do PedidoRepository). Um histórico nesse status tiraria unidades da
-- vitrine e quebraria os cenários de estoque do seed principal — a
-- interface com 2 unidades, por exemplo. Os status "fora do cálculo" são
-- APROVADA, EM_TRANSPORTE e REPROVADA, que não reservam nada.
--
-- POR QUE OS NÚMEROS PED-9000x
-- A sequence seq_numero_pedido continua sendo das compras reais, que
-- saem como PED-00001 em diante. Numerar o histórico a partir de 90001
-- evita colisão com a UNIQUE sem mexer na sequence.
--
-- O VALOR DE CADA VENDA
-- A medida do gráfico é o valor dos ITENS (quantidade × valor_unitario
-- de item_pedido), sem o frete: o frete é do pedido inteiro e não tem
-- como ser atribuído a uma categoria. Os pedidos abaixo têm frete e total
-- coerentes com a fórmula da RF0034 (R$ 15 + R$ 5 por peça adicional,
-- todos os endereços em SP), para a tela de pedidos não mostrar nada
-- inconsistente.
--
-- O JOGO DE CORDAS (INST-0005) ESTÁ EM DUAS CATEGORIAS
-- Acessórios e Guitarras (RN0012, seed principal). Uma venda de cordas
-- entra no total das DUAS. O total de cada linha do gráfico continua
-- correto; o que não se pode é somar as linhas entre si.
-- ============================================================

-- João não tem cartão no seed principal, e todo pedido precisa de uma
-- linha de pagamento para a tela de pedidos não mostrar "pago com" vazio.
-- O NOT EXISTS protege um banco de desenvolvimento em que alguém já tenha
-- cadastrado um cartão para ele: o índice ux_cartao_preferencial não
-- admitiria um segundo preferencial.
INSERT INTO cartao (cliente_id, bandeira_id, apelido, ultimos_digitos,
                    nome_titular, validade_mes, validade_ano, preferencial)
SELECT c.id, (SELECT id FROM bandeira WHERE nome = 'Visa'),
       'Cartão de João', '5544', 'JOAO PEREIRA', 8, 2030, TRUE
  FROM cliente c
 WHERE c.codigo = 'CLI-0003'
   AND NOT EXISTS (SELECT 1 FROM cartao WHERE cliente_id = c.id);

-- ------------------------------------------------------------
-- Pedidos
--
-- O horário leva o fuso de São Paulo de propósito. O PED-90017 está em
-- 30/09 às 23h30 (-03), que em UTC já é 1º de outubro: ele só cai em
-- setembro se a consulta agrupar pelo mês no fuso da loja, e não no do
-- servidor do banco. É o caso de borda que prova isso.
-- ------------------------------------------------------------
INSERT INTO pedido (numero, cliente_id, endereco_entrega_id, status,
                    valor_frete, valor_total, criado_em)
SELECT v.numero, c.id, e.id, v.status, v.frete, v.total, v.criado_em
  FROM (VALUES
    -- abril
    ('PED-90001', 'CLI-0002', 'ENTREGUE',      15.00, 1415.00, '2026-04-08 10:00:00-03'::timestamptz),
    ('PED-90002', 'CLI-0003', 'ENTREGUE',      20.00, 1420.00, '2026-04-15 14:30:00-03'::timestamptz),
    ('PED-90003', 'CLI-0002', 'REPROVADA',     15.00, 2815.00, '2026-04-22 09:15:00-03'::timestamptz),
    -- maio
    ('PED-90004', 'CLI-0003', 'ENTREGUE',      15.00, 2815.00, '2026-05-06 11:00:00-03'::timestamptz),
    ('PED-90005', 'CLI-0002', 'ENTREGUE',      30.00, 1535.00, '2026-05-14 16:45:00-03'::timestamptz),
    ('PED-90006', 'CLI-0003', 'EM_TRANSPORTE', 15.00,  715.00, '2026-05-27 10:20:00-03'::timestamptz),
    -- junho
    ('PED-90007', 'CLI-0002', 'ENTREGUE',      15.00, 1275.00, '2026-06-03 13:00:00-03'::timestamptz),
    ('PED-90008', 'CLI-0003', 'ENTREGUE',      15.00,  715.00, '2026-06-18 15:10:00-03'::timestamptz),
    ('PED-90009', 'CLI-0002', 'APROVADA',      15.00, 1415.00, '2026-06-25 08:40:00-03'::timestamptz),
    -- julho
    ('PED-90010', 'CLI-0003', 'ENTREGUE',      20.00, 2820.00, '2026-07-09 10:30:00-03'::timestamptz),
    ('PED-90011', 'CLI-0002', 'ENTREGUE',      20.00, 3520.00, '2026-07-21 17:00:00-03'::timestamptz),
    -- agosto
    ('PED-90012', 'CLI-0002', 'ENTREGUE',      35.00,  210.00, '2026-08-12 12:00:00-03'::timestamptz),
    ('PED-90013', 'CLI-0003', 'REPROVADA',     15.00, 1275.00, '2026-08-19 19:25:00-03'::timestamptz),
    -- setembro
    ('PED-90014', 'CLI-0003', 'ENTREGUE',      20.00, 2120.00, '2026-09-04 09:00:00-03'::timestamptz),
    ('PED-90015', 'CLI-0002', 'ENTREGUE',      15.00, 1275.00, '2026-09-16 14:00:00-03'::timestamptz),
    ('PED-90016', 'CLI-0002', 'EM_TRANSPORTE', 15.00, 2815.00, '2026-09-23 10:50:00-03'::timestamptz),
    ('PED-90017', 'CLI-0003', 'ENTREGUE',      15.00,  715.00, '2026-09-30 23:30:00-03'::timestamptz)
  ) AS v (numero, cliente, status, frete, total, criado_em)
  JOIN cliente  c ON c.codigo = v.cliente
  JOIN endereco e ON e.cliente_id = c.id AND e.principal;

-- ------------------------------------------------------------
-- Itens
--
-- O valor unitário vai explícito, e não lido de instrumento.valor_venda:
-- é a fotografia da compra (RF0038), e os totais desta migration não
-- podem depender do preço que o catálogo tiver no dia em que ela rodar.
-- ------------------------------------------------------------
INSERT INTO item_pedido (pedido_id, instrumento_id, instrumento_nome, quantidade, valor_unitario)
SELECT p.id, i.id, i.nome, v.quantidade, v.valor_unitario
  FROM (VALUES
    ('PED-90001', 'INST-0001', 1, 1400.00),
    ('PED-90002', 'INST-0003', 2,  700.00),
    ('PED-90003', 'INST-0002', 1, 2800.00),
    ('PED-90004', 'INST-0002', 1, 2800.00),
    ('PED-90005', 'INST-0001', 1, 1400.00),
    ('PED-90005', 'INST-0005', 3,   35.00),
    ('PED-90006', 'INST-0003', 1,  700.00),
    ('PED-90007', 'INST-0004', 1, 1260.00),
    ('PED-90008', 'INST-0003', 1,  700.00),
    ('PED-90009', 'INST-0001', 1, 1400.00),
    ('PED-90010', 'INST-0001', 2, 1400.00),
    ('PED-90011', 'INST-0002', 1, 2800.00),
    ('PED-90011', 'INST-0003', 1,  700.00),
    ('PED-90012', 'INST-0005', 5,   35.00),
    ('PED-90013', 'INST-0004', 1, 1260.00),
    ('PED-90014', 'INST-0001', 1, 1400.00),
    ('PED-90014', 'INST-0003', 1,  700.00),
    ('PED-90015', 'INST-0004', 1, 1260.00),
    ('PED-90016', 'INST-0002', 1, 2800.00),
    ('PED-90017', 'INST-0003', 1,  700.00)
  ) AS v (numero, codigo, quantidade, valor_unitario)
  JOIN pedido      p ON p.numero = v.numero
  JOIN instrumento i ON i.codigo = v.codigo;

-- ------------------------------------------------------------
-- Pagamento: uma linha de cartão com o total, no cartão preferencial do
-- cliente — o mesmo formato que a finalização grava (RF0036).
-- ------------------------------------------------------------
INSERT INTO pagamento (pedido_id, cartao_id, valor)
SELECT p.id, ca.id, p.valor_total
  FROM pedido p
  JOIN cartao ca ON ca.cliente_id = p.cliente_id AND ca.preferencial
 WHERE p.numero BETWEEN 'PED-90001' AND 'PED-90017';

-- Os JOINs acima descartariam em silêncio um pedido cujo cliente não
-- tivesse endereço principal ou cartão preferencial — e o gráfico ficaria
-- errado sem que ninguém soubesse. Melhor falhar aqui.
DO $$
BEGIN
    IF (SELECT count(*) FROM pedido WHERE numero BETWEEN 'PED-90001' AND 'PED-90017') <> 17 THEN
        RAISE EXCEPTION 'V1000: esperados 17 pedidos de histórico';
    END IF;
    IF (SELECT count(*) FROM item_pedido ip JOIN pedido p ON p.id = ip.pedido_id
         WHERE p.numero BETWEEN 'PED-90001' AND 'PED-90017') <> 20 THEN
        RAISE EXCEPTION 'V1000: esperados 20 itens de histórico';
    END IF;
    IF (SELECT count(*) FROM pagamento pg JOIN pedido p ON p.id = pg.pedido_id
         WHERE p.numero BETWEEN 'PED-90001' AND 'PED-90017') <> 17 THEN
        RAISE EXCEPTION 'V1000: esperada uma linha de pagamento por pedido de histórico';
    END IF;
END $$;

-- ============================================================
-- Valores esperados no gráfico — conferir antes de mudar qualquer linha
--
-- Somente pedidos ENTREGUE, valor dos itens, agrupado por mês da compra.
--
--   Categoria             abr      mai      jun      jul      ago      set
--   Guitarras           1400,00  1505,00     0,00  2800,00   175,00  1400,00
--   Teclados               0,00  2800,00     0,00  2800,00     0,00     0,00
--   Pedais              1400,00     0,00   700,00   700,00     0,00  1400,00
--   Interfaces de Áudio    0,00     0,00  1260,00     0,00     0,00  1260,00
--   Acessórios             0,00   105,00     0,00     0,00   175,00     0,00
--
-- Guitarras em maio e agosto inclui as cordas (multicategoria). Pedais em
-- setembro inclui o PED-90017, o caso de borda de fuso.
--
-- O que entraria se o filtro de status falhasse (cenário 5 do roteiro):
--   abr  Teclados             + 2800,00  (PED-90003, REPROVADA)
--   mai  Pedais               +  700,00  (PED-90006, EM_TRANSPORTE)
--   jun  Guitarras            + 1400,00  (PED-90009, APROVADA)
--   ago  Interfaces de Áudio  + 1260,00  (PED-90013, REPROVADA)
--   set  Teclados             + 2800,00  (PED-90016, EM_TRANSPORTE)
-- ============================================================
