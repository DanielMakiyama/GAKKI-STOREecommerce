package com.gakki.store.dto.request;

import com.gakki.store.domain.recomendacao.enums.EstiloMusical;
import com.gakki.store.domain.recomendacao.enums.NivelMusical;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.Set;

// RNF0044 — preferências musicais informadas no perfil. Orçamento é opcional.
public record PreferenciaClienteRequest(

        @NotNull(message = "Informe o nível musical.")
        NivelMusical nivel,

        @DecimalMin(value = "0.01", message = "O orçamento deve ser maior que zero.")
        @Digits(integer = 10, fraction = 2, message = "Orçamento inválido.")
        BigDecimal orcamentoMaximo,

        @Size(max = 300, message = "As observações têm no máximo 300 caracteres.")
        String observacoes,

        @NotEmpty(message = "Escolha ao menos uma categoria.")
        Set<@NotNull Long> categoriaIds,

        @NotEmpty(message = "Escolha ao menos um estilo musical.")
        @Size(max = 5, message = "Escolha no máximo 5 estilos musicais.")
        Set<@NotNull EstiloMusical> estilos) {
}
