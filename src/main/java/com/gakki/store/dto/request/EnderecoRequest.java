package com.gakki.store.dto.request;

import com.gakki.store.domain.enums.TipoLogradouro;
import com.gakki.store.domain.enums.TipoResidencia;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * RN0023 — composição do endereço.
 *
 * <p>Todos os campos são obrigatórios exceto {@code complemento} e
 * {@code observacoes}; a regra cita observações como o único opcional, e
 * complemento é ausência legítima (casa sem apartamento).
 *
 * <p>As marcações {@code entrega} e {@code cobranca} são ignoradas no
 * cadastro inicial: lá o endereço nasce servindo aos dois fins, que é o
 * que satisfaz a RN0021 e a RN0022 com um registro só.
 */
public record EnderecoRequest(

        @NotBlank(message = "Informe um apelido para o endereço")
        @Size(max = 40, message = "O apelido deve ter no máximo 40 caracteres")
        String apelido,

        @NotNull(message = "Informe o tipo de residência")
        TipoResidencia tipoResidencia,

        @NotNull(message = "Informe o tipo de logradouro")
        TipoLogradouro tipoLogradouro,

        @NotBlank(message = "Informe o logradouro")
        @Size(max = 150, message = "O logradouro deve ter no máximo 150 caracteres")
        String logradouro,

        @NotBlank(message = "Informe o número")
        @Size(max = 10, message = "O número deve ter no máximo 10 caracteres")
        String numero,

        @Size(max = 60, message = "O complemento deve ter no máximo 60 caracteres")
        String complemento,

        @NotBlank(message = "Informe o bairro")
        @Size(max = 80, message = "O bairro deve ter no máximo 80 caracteres")
        String bairro,

        @NotBlank(message = "Informe o CEP")
        @Pattern(regexp = "^[0-9]{5}-?[0-9]{3}$", message = "CEP inválido")
        String cep,

        @NotBlank(message = "Informe a cidade")
        @Size(max = 80, message = "A cidade deve ter no máximo 80 caracteres")
        String cidade,

        @NotBlank(message = "Informe o estado")
        @Pattern(regexp = "^[A-Za-z]{2}$", message = "O estado deve ser a sigla de 2 letras, ex.: SP")
        String estado,

        @NotBlank(message = "Informe o país")
        @Size(max = 60, message = "O país deve ter no máximo 60 caracteres")
        String pais,

        @Size(max = 255, message = "As observações devem ter no máximo 255 caracteres")
        String observacoes,

        boolean entrega,

        boolean cobranca) {
}
