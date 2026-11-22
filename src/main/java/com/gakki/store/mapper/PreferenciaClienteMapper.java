package com.gakki.store.mapper;

import com.gakki.store.domain.cliente.Cliente;
import com.gakki.store.domain.recomendacao.PreferenciaCliente;
import com.gakki.store.domain.recomendacao.enums.EstiloMusical;
import com.gakki.store.domain.vendas.Categoria;
import com.gakki.store.dto.request.PreferenciaClienteRequest;
import com.gakki.store.dto.response.CategoriaResponse;
import com.gakki.store.dto.response.PreferenciaClienteResponse;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Component
public class PreferenciaClienteMapper {

    public PreferenciaCliente novaPara(Cliente cliente) {
        PreferenciaCliente preferencia = new PreferenciaCliente();
        preferencia.setCliente(cliente);
        return preferencia;
    }

    public void aplicar(PreferenciaClienteRequest requisicao, PreferenciaCliente preferencia,
                        Set<Categoria> categorias) {
        preferencia.setNivel(requisicao.nivel());
        preferencia.setOrcamentoMaximo(requisicao.orcamentoMaximo());
        preferencia.setObservacoes(textoOuNulo(requisicao.observacoes()));
        preferencia.substituirCategorias(categorias);
        preferencia.substituirEstilos(requisicao.estilos());
    }

    public PreferenciaClienteResponse paraResponse(PreferenciaCliente preferencia) {
        List<CategoriaResponse> categorias = preferencia.getCategorias().stream()
                .sorted(Comparator.comparing(Categoria::getNome))
                .map(categoria -> new CategoriaResponse(categoria.getId(), categoria.getNome()))
                .toList();

        // Ordem do enum, para a tela mostrar sempre na mesma sequência.
        List<EstiloMusical> estilos = preferencia.getEstilos().stream().sorted().toList();

        return new PreferenciaClienteResponse(
                preferencia.getNivel(),
                preferencia.getOrcamentoMaximo(),
                preferencia.getObservacoes(),
                categorias,
                estilos,
                preferencia.getAtualizadoEm());
    }

    private String textoOuNulo(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
