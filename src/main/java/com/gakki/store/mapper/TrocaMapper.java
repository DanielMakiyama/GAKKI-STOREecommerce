package com.gakki.store.mapper;

import com.gakki.store.domain.cliente.Notificacao;
import com.gakki.store.domain.vendas.ItemTroca;
import com.gakki.store.domain.vendas.Troca;
import com.gakki.store.dto.response.ItemTrocaResponse;
import com.gakki.store.dto.response.NotificacaoResponse;
import com.gakki.store.dto.response.TrocaResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/*
 * Conversor do fluxo de troca. Sem repository injetado, como todos os
 * mappers do projeto: quem resolve entidade é o service.
 */
@Component
@RequiredArgsConstructor
public class TrocaMapper {

    private final CupomMapper cupomMapper;

    public TrocaResponse paraResponse(Troca troca) {
        List<ItemTrocaResponse> itens = troca.getItens().stream()
                .map(this::paraResponse)
                .toList();

        return new TrocaResponse(
                troca.getId(),
                troca.getNumero(),
                troca.getStatus().name(),
                troca.getPedido().getNumero(),
                troca.getCliente().getNome(),
                troca.getValor(),
                troca.getRetornaEstoque(),
                troca.getCupom() == null ? null : cupomMapper.paraResponse(troca.getCupom()),
                troca.getCriadoEm(),
                itens);
    }

    /*
     * O nome do instrumento vem do item do pedido, que é a fotografia da
     * compra — e não do cadastro atual. Uma troca pedida hoje sobre uma
     * compra de meses atrás tem de mostrar o nome que o produto tinha na
     * nota, mesmo que o catálogo já o tenha renomeado.
     */
    public ItemTrocaResponse paraResponse(ItemTroca item) {
        return new ItemTrocaResponse(
                item.getId(),
                item.getItemPedido().getId(),
                item.getItemPedido().getInstrumentoNome(),
                item.getQuantidade(),
                item.getItemPedido().getValorUnitario());
    }

    public NotificacaoResponse paraResponse(Notificacao notificacao) {
        return new NotificacaoResponse(
                notificacao.getId(),
                notificacao.getMensagem(),
                notificacao.isLida(),
                notificacao.getCriadoEm());
    }
}
