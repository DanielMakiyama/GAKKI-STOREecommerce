package com.gakki.store.mapper;

import com.gakki.store.domain.vendas.Carrinho;
import com.gakki.store.domain.vendas.Instrumento;
import com.gakki.store.domain.vendas.ItemCarrinho;
import com.gakki.store.dto.response.CarrinhoResponse;
import com.gakki.store.dto.response.ItemCarrinhoResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Component
public class CarrinhoMapper {

    // O instrumento chega resolvido como entidade, não como id — mesma
    // divisão do CartaoMapper: quem busca e valida é o service, para o
    // mapper não precisar de repository.
    //
    // O valor unitário é copiado do instrumento AGORA e não é lido de
    // novo depois: se o preço mudar, o que já está no carrinho não muda
    // debaixo do cliente.
    public ItemCarrinho novoItem(Instrumento instrumento, Integer quantidade) {
        ItemCarrinho item = new ItemCarrinho();
        item.setInstrumento(instrumento);
        item.setQuantidade(quantidade);
        item.setValorUnitario(instrumento.getValorVenda());
        return item;
    }

    public CarrinhoResponse paraResponse(Carrinho carrinho) {
        List<ItemCarrinhoResponse> itens = carrinho.getItens().stream()
                // Ordem estável por id: sem isso a lista pode trocar de
                // ordem entre dois GETs, e a tela "pula" sozinha.
                .sorted(Comparator.comparing(ItemCarrinho::getId,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .map(this::paraResponse)
                .toList();

        return new CarrinhoResponse(carrinho.getId(), itens, totalDe(carrinho));
    }

    public ItemCarrinhoResponse paraResponse(ItemCarrinho item) {
        return new ItemCarrinhoResponse(
                item.getId(),
                item.getInstrumento().getId(),
                item.getInstrumento().getNome(),
                item.getQuantidade(),
                item.getValorUnitario());
    }

    // Soma dos itens, sem frete.
    public BigDecimal totalDe(Carrinho carrinho) {
        return carrinho.getItens().stream()
                .map(item -> item.getValorUnitario()
                        .multiply(BigDecimal.valueOf(item.getQuantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
