package com.gakki.store.dto.response;

import java.util.List;

// versaoAtiva nula = nenhum ajuste fino ativo, o chat usa o modelo base.
public record IaStatusResponse(
        String provedor,
        String url,
        boolean online,
        String modeloEmUso,
        Integer versaoAtiva,
        boolean modeloInstalado,
        List<String> modelosInstalados) {
}
