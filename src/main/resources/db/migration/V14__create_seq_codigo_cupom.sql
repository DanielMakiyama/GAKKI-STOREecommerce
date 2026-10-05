-- Sequence do código do cupom de troco (RN0036).
--
-- Mesmo padrão de seq_codigo_cliente (V2) e seq_numero_pedido (V10): o
-- service consome o nextval e formata como TROCA-1000. Contador em
-- memória reiniciaria com a aplicação e colidiria entre duas instâncias —
-- e código de cupom é dinheiro, não pode repetir.
--
-- START WITH 1000 de propósito: os cupons do seed (V999) usam códigos
-- fixos de quatro dígitos a partir de 0001, e a sequence precisa começar
-- acima deles para nunca gerar um código já existente. O número também
-- deixa óbvio, lendo o banco, qual cupom veio do seed e qual o sistema
-- emitiu durante a demonstração.

CREATE SEQUENCE seq_codigo_cupom START WITH 1000 INCREMENT BY 1;

COMMENT ON SEQUENCE seq_codigo_cupom IS 'Numeração dos cupons de troco emitidos pelo troco de pagamento (RN0036)';
