-- ============================================================
-- Limpeza da massa gerada pelas execuções da suíte E2E
--
-- Devolve o banco ao estado que a V999 deixa, SEM derrubar o schema e sem
-- reexecutar o Flyway. Serve para rodar a suíte de novo, ou para ensaiar a
-- apresentação, sem acumular pedidos e clientes de execuções anteriores.
--
-- Uso:
--   docker exec -i gakki-db psql -U gakki -d gakki -v ON_ERROR_STOP=1 \
--     < backend/scripts/limpar_dados_de_teste.sql
--
-- É IDEMPOTENTE: rodar duas vezes seguidas dá o mesmo resultado.
--
-- ATENÇÃO: apaga TODOS os pedidos, não só os dos testes. Pedido feito à
-- mão no Postman ou na tela também vai junto — nesta fase não existe
-- pedido no seed, então "todo pedido" e "pedido gerado" são a mesma coisa.
--
-- O que este script NÃO precisa fazer, e por quê:
--   * Repor estoque. Nada neste fluxo decrementa
--     instrumento.quantidade_estoque — a baixa (RF0053, RN0028) está fora
--     do escopo da fatia. A RN0031 só COMPARA o pedido com o saldo. Por
--     isso a suíte pode rodar mil vezes sem esgotar a Focusrite.
--   * Recriar endereço ou cartão do seed. Nenhum caso remove linha do
--     CLI-0001: a RF0026 testa a RECUSA da remoção do último endereço de
--     cobrança, então a linha continua lá. O que muda são as marcações, e
--     elas são restauradas no passo 7.
-- ============================================================

BEGIN;

-- ------------------------------------------------------------
-- 1. Pagamentos e itens de pedido
--
-- Primeiro as folhas: pagamento aponta para pedido, cartão e cupom, e
-- item_pedido aponta para pedido. Apagar pedido antes violaria as FKs.
-- ------------------------------------------------------------
DELETE FROM pagamento;
DELETE FROM item_pedido;

-- ------------------------------------------------------------
-- 2. Soltar os cupons do pedido
--
-- cupom.pedido_utilizacao_id referencia pedido. Enquanto estiver
-- preenchido, nenhum pedido pode ser apagado. Zerar aqui já devolve os
-- cupons do seed à condição de "não utilizado", que é o que o roteiro
-- espera — PROMO-0001 e os três TROCA são de uso único.
-- ------------------------------------------------------------
UPDATE cupom
   SET utilizado            = FALSE,
       pedido_utilizacao_id = NULL;

-- ------------------------------------------------------------
-- 3. Pedidos
-- ------------------------------------------------------------
DELETE FROM pedido;

-- ------------------------------------------------------------
-- 4. Cupons que não vieram do seed
--
-- Dois tipos nascem durante a execução:
--   * E2E-<millis>-<n>  — criados por JDBC pelo BancoDeApoio, porque
--                         cupom é de uso único e não existe endpoint de
--                         emissão (se existisse, cliente emitiria
--                         dinheiro para si).
--   * TROCA-1000 em diante — troco emitido pela RN0036. A sequence
--                         seq_codigo_cupom começa em 1000 justamente para
--                         não colidir com os códigos de quatro dígitos do
--                         seed, o que torna a distinção visível no banco.
--
-- Precisa vir ANTES de apagar os clientes: cupom.cliente_id é FK.
-- ------------------------------------------------------------
DELETE FROM cupom
 WHERE codigo NOT IN ('TROCA-0001', 'TROCA-0002', 'TROCA-0003', 'PROMO-0001');

-- ------------------------------------------------------------
-- 5. Carrinhos
--
-- O carrinho é 1:1 com o cliente e persiste entre execuções — é por isso
-- que o BaseVendasE2ETest esvazia o carrinho no @BeforeEach. Aqui some a
-- linha inteira; o service recria sob demanda na primeira adição.
-- ------------------------------------------------------------
DELETE FROM item_carrinho;
DELETE FROM carrinho;

-- ------------------------------------------------------------
-- 6. Clientes criados durante os testes
--
-- O critério é o CÓDIGO, não o e-mail. O GeradorDeDados monta
-- e2e-<millis>-<n>@example.com, mas a RF0022 troca o e-mail do cliente
-- durante o próprio teste — filtrar por e-mail deixaria essa linha para
-- trás. O código CLI-0001..CLI-0004 é do seed e nunca muda.
--
-- Da folha para a raiz: cartão e endereço apontam para cliente, e cliente
-- aponta para usuário.
-- ------------------------------------------------------------
DELETE FROM cartao
 WHERE cliente_id IN (SELECT id FROM cliente
                       WHERE codigo NOT IN ('CLI-0001','CLI-0002','CLI-0003','CLI-0004'));

DELETE FROM endereco
 WHERE cliente_id IN (SELECT id FROM cliente
                       WHERE codigo NOT IN ('CLI-0001','CLI-0002','CLI-0003','CLI-0004'));

DELETE FROM cliente
 WHERE codigo NOT IN ('CLI-0001','CLI-0002','CLI-0003','CLI-0004');

-- Os usuários ficam órfãos quando o cliente sai. O cadastro cria os dois
-- na mesma transação, então todo usuário de papel CLIENTE sem linha em
-- `cliente` veio de um teste. O administrador não entra nesta conta — ele
-- não tem cadastro de cliente por definição.
DELETE FROM usuario u
 WHERE u.papel = 'CLIENTE'
   AND NOT EXISTS (SELECT 1 FROM cliente c WHERE c.usuario_id = u.id);

-- ------------------------------------------------------------
-- 7. Endereços e cartões extras dos clientes do seed
--
-- A RF0035 cadastra um endereço e um cartão NOVOS durante o checkout, no
-- perfil do CLI-0001 — e eles ficam lá, que é justamente o que o teste
-- verifica. A cada execução sobra mais um par.
--
-- O par (código do cliente, apelido) identifica exatamente o que a V999
-- inseriu. Se o seed mudar, esta lista muda junto.
-- ------------------------------------------------------------
DELETE FROM endereco e
 USING cliente c
 WHERE c.id = e.cliente_id
   AND (c.codigo, e.apelido) NOT IN (
         ('CLI-0001', 'Casa'), ('CLI-0001', 'Trabalho'),
         ('CLI-0002', 'Casa'),
         ('CLI-0003', 'Casa'),
         ('CLI-0004', 'Casa'));

DELETE FROM cartao ct
 USING cliente c
 WHERE c.id = ct.cliente_id
   AND (c.codigo, ct.apelido) NOT IN (
         ('CLI-0001', 'Cartão principal'), ('CLI-0001', 'Cartão reserva'),
         ('CLI-0002', 'Cartão de Maria'));

-- ------------------------------------------------------------
-- 8. Marcações de volta ao estado do seed
--
-- ux_endereco_principal é um índice único PARCIAL (um principal por
-- cliente), então é obrigatório desmarcar todos antes de marcar o certo —
-- senão duas linhas ficariam principal ao mesmo tempo no meio do UPDATE.
-- Mesma coisa para ux_cartao_preferencial.
-- ------------------------------------------------------------
UPDATE endereco SET principal = FALSE;
UPDATE cartao   SET preferencial = FALSE;

UPDATE endereco e
   SET entrega = TRUE, cobranca = TRUE, principal = TRUE
  FROM cliente c
 WHERE c.id = e.cliente_id
   AND (c.codigo, e.apelido) IN (
         ('CLI-0001', 'Casa'),
         ('CLI-0002', 'Casa'), ('CLI-0003', 'Casa'), ('CLI-0004', 'Casa'));

UPDATE endereco e
   SET entrega = TRUE, cobranca = FALSE, principal = FALSE
  FROM cliente c
 WHERE c.id = e.cliente_id
   AND c.codigo = 'CLI-0001' AND e.apelido = 'Trabalho';

UPDATE cartao ct
   SET preferencial = TRUE
  FROM cliente c
 WHERE c.id = ct.cliente_id
   AND (c.codigo, ct.apelido) IN (
         ('CLI-0001', 'Cartão principal'),
         ('CLI-0002', 'Cartão de Maria'));

-- Beatriz nasce inativa no seed para a RF0023 e a RF0024 terem um
-- cadastro inativo sem precisar inativar ninguém ao vivo. Os demais
-- nascem ativos. Restaurar protege contra uma inativação feita à mão
-- durante um ensaio.
UPDATE usuario
   SET ativo = (email <> 'beatriz.lima@example.com')
 WHERE email IN ('cliente@gakkistore.com', 'admin@gakkistore.com',
                 'maria.souza@example.com', 'joao.pereira@example.com',
                 'beatriz.lima@example.com');

-- ------------------------------------------------------------
-- 9. Log de transação (RNF0012)
--
-- O log é prova de auditoria, não dado de negócio: ele registra escritas
-- que acabaram de ser desfeitas por este script. Manter as linhas
-- apontando para pedidos que não existem mais só confunde a demonstração
-- do próprio RNF0012.
-- ------------------------------------------------------------
DELETE FROM log_transacao;

-- ------------------------------------------------------------
-- 10. Sequences
--
-- setval(seq, n)        → o próximo nextval devolve n + 1
-- setval(seq, n, FALSE) → o próximo nextval devolve n
-- ------------------------------------------------------------
SELECT setval('seq_codigo_cliente',    4);           -- próximo: CLI-0005
SELECT setval('seq_numero_pedido',     1, FALSE);    -- próximo: PED-00001
SELECT setval('seq_codigo_cupom',      1000, FALSE); -- próximo: TROCA-1000

-- O seed grava setval(...,5) com SEIS instrumentos na base, então o
-- próximo instrumento criado pela API sairia como INST-0006 e colidiria
-- com a UNIQUE de código. Corrigido aqui no banco vivo; a V999 precisa da
-- mesma correção para o próximo rebuild.
SELECT setval('seq_codigo_instrumento', (SELECT count(*) FROM instrumento));

COMMIT;

-- ============================================================
-- Conferência — o resultado esperado está na coluna `esperado`
-- ============================================================
SELECT 'pedidos'                AS o_que, count(*)::text AS atual, '0' AS esperado FROM pedido
UNION ALL SELECT 'pagamentos',     count(*)::text, '0'  FROM pagamento
UNION ALL SELECT 'itens de pedido',count(*)::text, '0'  FROM item_pedido
UNION ALL SELECT 'carrinhos',      count(*)::text, '0'  FROM carrinho
UNION ALL SELECT 'linhas de log',  count(*)::text, '0'  FROM log_transacao
UNION ALL SELECT 'clientes',       count(*)::text, '4'  FROM cliente
UNION ALL SELECT 'endereços',      count(*)::text, '5'  FROM endereco
UNION ALL SELECT 'cartões',        count(*)::text, '3'  FROM cartao
UNION ALL SELECT 'cupons',         count(*)::text, '4'  FROM cupom
UNION ALL SELECT 'cupons livres',  count(*)::text, '4'  FROM cupom WHERE NOT utilizado
UNION ALL SELECT 'instrumentos',   count(*)::text, '6'  FROM instrumento;
