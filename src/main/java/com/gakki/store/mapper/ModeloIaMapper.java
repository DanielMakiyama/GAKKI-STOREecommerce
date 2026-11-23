package com.gakki.store.mapper;

import com.gakki.store.domain.recomendacao.ModeloIa;
import com.gakki.store.dto.response.ModeloIaResponse;
import org.springframework.stereotype.Component;

@Component
public class ModeloIaMapper {

    public ModeloIaResponse paraResponse(ModeloIa modelo) {
        return new ModeloIaResponse(
                modelo.getVersao(),
                modelo.getStatus(),
                modelo.isAtivo(),
                modelo.getProvedor(),
                modelo.getModeloBase(),
                modelo.getIdentificador(),
                modelo.getJobId(),
                modelo.getExemplosTotal(),
                modelo.getExemplosVendas(),
                modelo.getExemplosFeedback(),
                modelo.getDadosAte(),
                modelo.getIniciadoEm(),
                modelo.getConcluidoEm(),
                modelo.getErro(),
                "/ia/treinamentos/" + modelo.getVersao() + "/dados");
    }
}
