-- Índices da análise de vendas por categoria (RF0055, RNF0011).
--
-- A consulta do histórico é uma agregação só: filtra `pedido` por status
-- e período, desce para `item_pedido`, cruza com `instrumento_categoria`
-- e soma por categoria e mês. O RNF0011 exige resposta em até 1 segundo,
-- e a tabela de pedidos é a que mais cresce no sistema — sem índice, cada
-- consulta leria o histórico inteiro de vendas.
--
-- Migration de estrutura com número menor que o V999 já aplicado: roda
-- pelo `out-of-order: true` do application.yml, como a V15 e a V16.

-- Status primeiro, criado_em depois: a consulta compara o status por
-- igualdade e o período por intervalo, e é nessa ordem que um índice
-- composto atende os dois. Serve também ao `quantidadeComStatus` do
-- PedidoRepository (reserva de estoque dos pedidos EM_PROCESSAMENTO),
-- que filtra pelo mesmo status.
CREATE INDEX ix_pedido_status_criado_em ON pedido (status, criado_em);

-- A junção item → instrumento → categoria. A chave primária de
-- `instrumento_categoria` começa por `instrumento_id`, então já atende a
-- junção vinda do item; o índice por `categoria_id` atende o filtro pelas
-- categorias escolhidas na tela.
CREATE INDEX ix_item_pedido_instrumento        ON item_pedido (instrumento_id);
CREATE INDEX ix_instrumento_categoria_categoria ON instrumento_categoria (categoria_id);
