package com.gakki.store.domain.recomendacao.enums;

// Ciclo de uma versão do modelo: o conjunto de dados é montado
// (PREPARANDO), enviado ao provedor (TREINANDO) e termina PRONTO ou FALHOU.
public enum StatusModeloIa {
    PREPARANDO,
    TREINANDO,
    PRONTO,
    FALHOU
}
