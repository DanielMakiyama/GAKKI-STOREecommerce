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
        // getNome() no fabricante e a leitura das categorias só não
        // disparam consulta porque quem chama usa @EntityGraph. Mapper
        // não busca nada por conta própria — é conversor, não camada de
        // acesso a dados.
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
