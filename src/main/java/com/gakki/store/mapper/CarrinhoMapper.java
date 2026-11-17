package com.gakki.store.mapper;

import com.gakki.store.domain.vendas.Carrinho;
import com.gakki.store.domain.vendas.Instrumento;
import com.gakki.store.domain.vendas.ItemCarrinho;
import com.gakki.store.domain.vendas.ItemRetirado;
import com.gakki.store.dto.response.CarrinhoResponse;
import com.gakki.store.dto.response.ItemCarrinhoResponse;
import com.gakki.store.dto.response.ItemRetiradoResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;

@Component
public class CarrinhoMapper {

    // O instrumento chega resolvido como entidade
    public ItemCarrinho novoItem(Instrumento instrumento, Integer quantidade) {
        ItemCarrinho item = new ItemCarrinho();
        item.setInstrumento(instrumento);
        item.setQuantidade(quantidade);
        item.setValorUnitario(instrumento.getValorVenda());
        return item;
    }

    //RNF0042 - além dos itens, a resposta carrega o estado do bloqueio e o que saiu por expiração.
    public CarrinhoResponse paraResponse(Carrinho carrinho,
                                         List<ItemRetirado> retirados,
                                         Duration prazoBloqueio) {

        List<ItemCarrinhoResponse> itens = carrinho.getItens().stream()
                .sorted(Comparator.comparing(ItemCarrinho::getId,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .map(this::paraResponse)
                .toList();

        List<ItemRetiradoResponse> retiradosResposta = retirados.stream()
                .map(this::paraResponse)
                .toList();

        return new CarrinhoResponse(
                carrinho.getId(),
                itens,
                totalDe(carrinho),
                carrinho.getBloqueadoAte(),
                prazoBloqueio.toSeconds(),
                retiradosResposta,
                !itens.isEmpty() && retiradosResposta.isEmpty());
    }

    public ItemCarrinhoResponse paraResponse(ItemCarrinho item) {
        return new ItemCarrinhoResponse(
                item.getId(),
                item.getInstrumento().getId(),
                item.getInstrumento().getNome(),
                item.getQuantidade(),
                item.getValorUnitario());
    }

    public ItemRetiradoResponse paraResponse(ItemRetirado item) {
        return new ItemRetiradoResponse(
                item.getId(),
                item.getInstrumento().getId(),
                item.getInstrumento().getNome(),
                item.getQuantidade(),
                item.getValorUnitario(),
                item.getRetiradoEm());
    }

    // Soma dos itens, sem frete.
    public BigDecimal totalDe(Carrinho carrinho) {
        return carrinho.getItens().stream()
                .map(item -> item.getValorUnitario()
                        .multiply(BigDecimal.valueOf(item.getQuantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
