-- Massa de dados ampliada da análise de vendas (RF0055): histórico desde 2025.
-- Cobre jan/2025 a mar/2026 e out a nov/2026. Abril a setembro de 2026 ficam só
-- com a V1000, que é o gabarito dos testes E2E da RF0055.
-- Mesmas regras da V1000: clientes CLI-0002 e CLI-0003, nenhum pedido
-- EM_PROCESSAMENTO (reservaria estoque), frete de R$ 15 + R$ 5 por peça adicional.

INSERT INTO pedido (numero, cliente_id, endereco_entrega_id, status,
                    valor_frete, valor_total, criado_em)
SELECT v.numero, c.id, e.id, v.status, v.frete, v.total, v.criado_em
  FROM (VALUES
    ('PED-91001', 'CLI-0003', 'ENTREGUE', 15.00, 1275.00, '2025-01-04 10:05:00-03'::timestamptz),
    ('PED-91002', 'CLI-0003', 'ENTREGUE', 15.00, 1415.00, '2025-01-10 20:00:00-03'::timestamptz),
    ('PED-91003', 'CLI-0002', 'ENTREGUE', 20.00, 1735.00, '2025-02-17 21:05:00-03'::timestamptz),
    ('PED-91004', 'CLI-0002', 'ENTREGUE', 25.00, 1495.00, '2025-02-18 21:50:00-03'::timestamptz),
    ('PED-91005', 'CLI-0003', 'ENTREGUE', 25.00, 1495.00, '2025-03-07 18:55:00-03'::timestamptz),
    ('PED-91006', 'CLI-0003', 'ENTREGUE', 20.00, 2120.00, '2025-03-09 09:45:00-03'::timestamptz),
    ('PED-91007', 'CLI-0002', 'ENTREGUE', 20.00, 1735.00, '2025-03-26 10:15:00-03'::timestamptz),
    ('PED-91008', 'CLI-0003', 'ENTREGUE', 25.00, 130.00, '2025-04-03 08:20:00-03'::timestamptz),
    ('PED-91009', 'CLI-0002', 'ENTREGUE', 20.00, 1420.00, '2025-04-09 14:30:00-03'::timestamptz),
    ('PED-91010', 'CLI-0002', 'ENTREGUE', 15.00, 2815.00, '2025-04-30 11:20:00-03'::timestamptz),
    ('PED-91011', 'CLI-0003', 'ENTREGUE', 15.00, 1415.00, '2025-05-02 18:20:00-03'::timestamptz),
    ('PED-91012', 'CLI-0003', 'ENTREGUE', 15.00, 715.00, '2025-05-26 10:50:00-03'::timestamptz),
    ('PED-91013', 'CLI-0002', 'ENTREGUE', 25.00, 1495.00, '2025-05-27 18:50:00-03'::timestamptz),
    ('PED-91014', 'CLI-0003', 'ENTREGUE', 15.00, 715.00, '2025-06-06 18:40:00-03'::timestamptz),
    ('PED-91015', 'CLI-0002', 'ENTREGUE', 25.00, 1495.00, '2025-06-09 19:15:00-03'::timestamptz),
    ('PED-91016', 'CLI-0002', 'ENTREGUE', 15.00, 1695.00, '2025-06-12 09:25:00-03'::timestamptz),
    ('PED-91017', 'CLI-0002', 'ENTREGUE', 15.00, 1415.00, '2025-07-01 22:20:00-03'::timestamptz),
    ('PED-91018', 'CLI-0002', 'ENTREGUE', 15.00, 1275.00, '2025-07-10 13:20:00-03'::timestamptz),
    ('PED-91019', 'CLI-0003', 'ENTREGUE', 15.00, 1695.00, '2025-07-15 21:45:00-03'::timestamptz),
    ('PED-91020', 'CLI-0003', 'ENTREGUE', 25.00, 1495.00, '2025-08-10 12:10:00-03'::timestamptz),
    ('PED-91021', 'CLI-0002', 'ENTREGUE', 25.00, 1495.00, '2025-08-20 13:45:00-03'::timestamptz),
    ('PED-91022', 'CLI-0003', 'ENTREGUE', 15.00, 715.00, '2025-09-02 20:25:00-03'::timestamptz),
    ('PED-91023', 'CLI-0003', 'ENTREGUE', 20.00, 4080.00, '2025-09-12 11:30:00-03'::timestamptz),
    ('PED-91024', 'CLI-0002', 'ENTREGUE', 15.00, 2815.00, '2025-10-02 17:00:00-03'::timestamptz),
    ('PED-91025', 'CLI-0003', 'ENTREGUE', 20.00, 1420.00, '2025-10-04 08:05:00-03'::timestamptz),
    ('PED-91026', 'CLI-0003', 'ENTREGUE', 25.00, 130.00, '2025-10-06 18:15:00-03'::timestamptz),
    ('PED-91027', 'CLI-0003', 'ENTREGUE', 15.00, 715.00, '2025-10-24 15:00:00-03'::timestamptz),
    ('PED-91028', 'CLI-0002', 'ENTREGUE', 15.00, 715.00, '2025-11-07 08:20:00-03'::timestamptz),
    ('PED-91029', 'CLI-0002', 'ENTREGUE', 25.00, 1495.00, '2025-11-08 14:20:00-03'::timestamptz),
    ('PED-91030', 'CLI-0003', 'REPROVADA', 15.00, 1415.00, '2025-11-14 17:05:00-03'::timestamptz),
    ('PED-91031', 'CLI-0002', 'ENTREGUE', 20.00, 4080.00, '2025-11-15 15:30:00-03'::timestamptz),
    ('PED-91032', 'CLI-0002', 'ENTREGUE', 15.00, 1415.00, '2025-11-21 13:20:00-03'::timestamptz),
    ('PED-91033', 'CLI-0003', 'ENTREGUE', 15.00, 2815.00, '2025-11-30 21:30:00-03'::timestamptz),
    ('PED-91034', 'CLI-0002', 'ENTREGUE', 20.00, 2120.00, '2025-12-07 21:20:00-03'::timestamptz),
    ('PED-91035', 'CLI-0002', 'ENTREGUE', 20.00, 2120.00, '2025-12-09 14:45:00-03'::timestamptz),
    ('PED-91036', 'CLI-0002', 'ENTREGUE', 15.00, 1415.00, '2025-12-11 21:10:00-03'::timestamptz),
    ('PED-91037', 'CLI-0003', 'ENTREGUE', 15.00, 1275.00, '2025-12-13 19:30:00-03'::timestamptz),
    ('PED-91038', 'CLI-0002', 'ENTREGUE', 20.00, 2120.00, '2025-12-21 11:10:00-03'::timestamptz),
    ('PED-91039', 'CLI-0002', 'ENTREGUE', 15.00, 1415.00, '2025-12-22 14:30:00-03'::timestamptz),
    ('PED-91040', 'CLI-0002', 'ENTREGUE', 35.00, 210.00, '2025-12-26 10:50:00-03'::timestamptz),
    ('PED-91041', 'CLI-0003', 'ENTREGUE', 15.00, 2815.00, '2025-12-29 22:40:00-03'::timestamptz),
    ('PED-91042', 'CLI-0002', 'ENTREGUE', 15.00, 1415.00, '2026-01-01 15:40:00-03'::timestamptz),
    ('PED-91043', 'CLI-0002', 'ENTREGUE', 35.00, 210.00, '2026-01-03 08:25:00-03'::timestamptz),
    ('PED-91044', 'CLI-0003', 'ENTREGUE', 20.00, 2120.00, '2026-02-17 21:25:00-03'::timestamptz),
    ('PED-91045', 'CLI-0003', 'REPROVADA', 25.00, 130.00, '2026-02-25 14:15:00-03'::timestamptz),
    ('PED-91046', 'CLI-0003', 'ENTREGUE', 15.00, 715.00, '2026-03-08 09:05:00-03'::timestamptz),
    ('PED-91047', 'CLI-0003', 'ENTREGUE', 15.00, 1415.00, '2026-03-13 16:30:00-03'::timestamptz),
    ('PED-91048', 'CLI-0003', 'ENTREGUE', 15.00, 715.00, '2026-03-25 09:50:00-03'::timestamptz),
    ('PED-91049', 'CLI-0002', 'ENTREGUE', 15.00, 715.00, '2026-10-10 20:40:00-03'::timestamptz),
    ('PED-91050', 'CLI-0002', 'ENTREGUE', 25.00, 1495.00, '2026-10-21 19:10:00-03'::timestamptz),
    ('PED-91051', 'CLI-0003', 'ENTREGUE', 15.00, 1695.00, '2026-10-27 16:00:00-03'::timestamptz),
    ('PED-91052', 'CLI-0003', 'REPROVADA', 15.00, 1275.00, '2026-10-30 09:40:00-03'::timestamptz),
    ('PED-91053', 'CLI-0003', 'ENTREGUE', 25.00, 1495.00, '2026-11-02 13:15:00-03'::timestamptz),
    ('PED-91054', 'CLI-0002', 'ENTREGUE', 15.00, 1275.00, '2026-11-05 18:00:00-03'::timestamptz),
    ('PED-91055', 'CLI-0003', 'EM_TRANSPORTE', 15.00, 2815.00, '2026-11-08 16:35:00-03'::timestamptz),
    ('PED-91056', 'CLI-0003', 'APROVADA', 15.00, 1275.00, '2026-11-10 22:40:00-03'::timestamptz)
  ) AS v (numero, cliente, status, frete, total, criado_em)
  JOIN cliente  c ON c.codigo = v.cliente
  JOIN endereco e ON e.cliente_id = c.id AND e.principal;

-- Valor unitário explícito: é a fotografia do preço na data da compra.
INSERT INTO item_pedido (pedido_id, instrumento_id, instrumento_nome, quantidade, valor_unitario)
SELECT p.id, i.id, i.nome, v.quantidade, v.valor_unitario
  FROM (VALUES
    ('PED-91001', 'INST-0004', 1, 1260.00),
    ('PED-91002', 'INST-0001', 1, 1400.00),
    ('PED-91003', 'INST-0006', 1, 1680.00),
    ('PED-91003', 'INST-0005', 1, 35.00),
    ('PED-91004', 'INST-0001', 1, 1400.00),
    ('PED-91004', 'INST-0005', 2, 35.00),
    ('PED-91005', 'INST-0001', 1, 1400.00),
    ('PED-91005', 'INST-0005', 2, 35.00),
    ('PED-91006', 'INST-0001', 1, 1400.00),
    ('PED-91006', 'INST-0003', 1, 700.00),
    ('PED-91007', 'INST-0006', 1, 1680.00),
    ('PED-91007', 'INST-0005', 1, 35.00),
    ('PED-91008', 'INST-0005', 3, 35.00),
    ('PED-91009', 'INST-0003', 2, 700.00),
    ('PED-91010', 'INST-0002', 1, 2800.00),
    ('PED-91011', 'INST-0001', 1, 1400.00),
    ('PED-91012', 'INST-0003', 1, 700.00),
    ('PED-91013', 'INST-0001', 1, 1400.00),
    ('PED-91013', 'INST-0005', 2, 35.00),
    ('PED-91014', 'INST-0003', 1, 700.00),
    ('PED-91015', 'INST-0001', 1, 1400.00),
    ('PED-91015', 'INST-0005', 2, 35.00),
    ('PED-91016', 'INST-0006', 1, 1680.00),
    ('PED-91017', 'INST-0001', 1, 1400.00),
    ('PED-91018', 'INST-0004', 1, 1260.00),
    ('PED-91019', 'INST-0006', 1, 1680.00),
    ('PED-91020', 'INST-0001', 1, 1400.00),
    ('PED-91020', 'INST-0005', 2, 35.00),
    ('PED-91021', 'INST-0001', 1, 1400.00),
    ('PED-91021', 'INST-0005', 2, 35.00),
    ('PED-91022', 'INST-0003', 1, 700.00),
    ('PED-91023', 'INST-0004', 1, 1260.00),
    ('PED-91023', 'INST-0002', 1, 2800.00),
    ('PED-91024', 'INST-0002', 1, 2800.00),
    ('PED-91025', 'INST-0003', 2, 700.00),
    ('PED-91026', 'INST-0005', 3, 35.00),
    ('PED-91027', 'INST-0003', 1, 700.00),
    ('PED-91028', 'INST-0003', 1, 700.00),
    ('PED-91029', 'INST-0001', 1, 1400.00),
    ('PED-91029', 'INST-0005', 2, 35.00),
    ('PED-91030', 'INST-0001', 1, 1400.00),
    ('PED-91031', 'INST-0004', 1, 1260.00),
    ('PED-91031', 'INST-0002', 1, 2800.00),
    ('PED-91032', 'INST-0001', 1, 1400.00),
    ('PED-91033', 'INST-0002', 1, 2800.00),
    ('PED-91034', 'INST-0001', 1, 1400.00),
    ('PED-91034', 'INST-0003', 1, 700.00),
    ('PED-91035', 'INST-0001', 1, 1400.00),
    ('PED-91035', 'INST-0003', 1, 700.00),
    ('PED-91036', 'INST-0001', 1, 1400.00),
    ('PED-91037', 'INST-0004', 1, 1260.00),
    ('PED-91038', 'INST-0001', 1, 1400.00),
    ('PED-91038', 'INST-0003', 1, 700.00),
    ('PED-91039', 'INST-0001', 1, 1400.00),
    ('PED-91040', 'INST-0005', 5, 35.00),
    ('PED-91041', 'INST-0002', 1, 2800.00),
    ('PED-91042', 'INST-0001', 1, 1400.00),
    ('PED-91043', 'INST-0005', 5, 35.00),
    ('PED-91044', 'INST-0001', 1, 1400.00),
    ('PED-91044', 'INST-0003', 1, 700.00),
    ('PED-91045', 'INST-0005', 3, 35.00),
    ('PED-91046', 'INST-0003', 1, 700.00),
    ('PED-91047', 'INST-0001', 1, 1400.00),
    ('PED-91048', 'INST-0003', 1, 700.00),
    ('PED-91049', 'INST-0003', 1, 700.00),
    ('PED-91050', 'INST-0001', 1, 1400.00),
    ('PED-91050', 'INST-0005', 2, 35.00),
    ('PED-91051', 'INST-0006', 1, 1680.00),
    ('PED-91052', 'INST-0004', 1, 1260.00),
    ('PED-91053', 'INST-0001', 1, 1400.00),
    ('PED-91053', 'INST-0005', 2, 35.00),
    ('PED-91054', 'INST-0004', 1, 1260.00),
    ('PED-91055', 'INST-0002', 1, 2800.00),
    ('PED-91056', 'INST-0004', 1, 1260.00)
  ) AS v (numero, codigo, quantidade, valor_unitario)
  JOIN pedido      p ON p.numero = v.numero
  JOIN instrumento i ON i.codigo = v.codigo;

INSERT INTO pagamento (pedido_id, cartao_id, valor)
SELECT p.id, ca.id, p.valor_total
  FROM pedido p
  JOIN cartao ca ON ca.cliente_id = p.cliente_id AND ca.preferencial
 WHERE p.numero BETWEEN 'PED-91001' AND 'PED-91056';

-- Falha se algum JOIN acima descartou linhas em silêncio.
DO $$
BEGIN
    IF (SELECT count(*) FROM pedido WHERE numero BETWEEN 'PED-91001' AND 'PED-91056') <> 56 THEN
        RAISE EXCEPTION 'V1001: esperados 56 pedidos';
    END IF;
    IF (SELECT count(*) FROM item_pedido ip JOIN pedido p ON p.id = ip.pedido_id
         WHERE p.numero BETWEEN 'PED-91001' AND 'PED-91056') <> 74 THEN
        RAISE EXCEPTION 'V1001: esperados 74 itens';
    END IF;
    IF (SELECT count(*) FROM pagamento pg JOIN pedido p ON p.id = pg.pedido_id
         WHERE p.numero BETWEEN 'PED-91001' AND 'PED-91056') <> 56 THEN
        RAISE EXCEPTION 'V1001: esperada uma linha de pagamento por pedido';
    END IF;
END $$;
