package com.gakki.store.dto.request;

import jakarta.validation.constraints.Size;

// jobId: referência livre do treino, como o nome ou o link do notebook no Colab.
public record IniciarTreinamentoRequest(

        @Size(max = 300, message = "A referência tem no máximo 300 caracteres.")
        String jobId) {
}
