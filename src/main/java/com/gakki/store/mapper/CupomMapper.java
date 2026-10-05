package com.gakki.store.mapper;

import com.gakki.store.domain.vendas.Cupom;
import com.gakki.store.dto.response.CupomResponse;
import org.springframework.stereotype.Component;

@Component
public class CupomMapper {

    public CupomResponse paraResponse(Cupom cupom) {
        return new CupomResponse(
                cupom.getId(),
                cupom.getCodigo(),
                // O enum vira String na borda: a tela imprime o texto
                // direto, e o nome do enum é o próprio rótulo.
                cupom.getTipo().name(),
                cupom.getValor(),
                cupom.isUtilizado());
    }
}
