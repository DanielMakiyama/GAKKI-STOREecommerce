package com.gakki.store.mapper;

import com.gakki.store.domain.vendas.Categoria;
import com.gakki.store.domain.vendas.Instrumento;
import com.gakki.store.dto.response.InstrumentoResponse;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class InstrumentoMapper {

    public InstrumentoResponse paraResponse(Instrumento instrumento) {

        List<String> categorias = instrumento.getCategorias().stream()
                .map(Categoria::getNome)
                .sorted(Comparator.naturalOrder())
                .toList();

        return new InstrumentoResponse(
                instrumento.getId(),
                instrumento.getCodigo(),
                instrumento.getNome(),
                instrumento.getDescricao(),
                instrumento.getFabricante().getNome(),
                instrumento.getAnoFabricacao(),
                instrumento.getValorVenda(),
                instrumento.getQuantidadeEstoque(),
                instrumento.isAtivo(),
                categorias);
    }
}
