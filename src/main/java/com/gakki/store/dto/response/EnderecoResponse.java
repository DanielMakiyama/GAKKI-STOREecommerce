package com.gakki.store.dto.response;

import com.gakki.store.domain.enums.TipoLogradouro;
import com.gakki.store.domain.enums.TipoResidencia;

public record EnderecoResponse(
        Long id,
        String apelido,
        TipoResidencia tipoResidencia,
        TipoLogradouro tipoLogradouro,
        String logradouro,
        String numero,
        String complemento,
        String bairro,
        String cep,
        String cidade,
        String estado,
        String pais,
        String observacoes,
        boolean entrega,
        boolean cobranca,
        boolean principal) {
}
