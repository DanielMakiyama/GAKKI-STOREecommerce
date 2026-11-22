package com.gakki.store.dto.response;

import com.gakki.store.domain.recomendacao.enums.EstiloMusical;
import com.gakki.store.domain.recomendacao.enums.NivelMusical;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record PreferenciaClienteResponse(
        NivelMusical nivel,
        BigDecimal orcamentoMaximo,
        String observacoes,
        List<CategoriaResponse> categorias,
        List<EstiloMusical> estilos,
        OffsetDateTime atualizadoEm) {
}
