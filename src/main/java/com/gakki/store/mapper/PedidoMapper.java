package com.gakki.store.mapper;

import com.gakki.store.domain.cliente.Cartao;
import com.gakki.store.domain.cliente.Endereco;
import com.gakki.store.domain.vendas.Cupom;
import com.gakki.store.domain.vendas.ItemCarrinho;
import com.gakki.store.domain.vendas.ItemPedido;
import com.gakki.store.domain.vendas.Pagamento;
import com.gakki.store.domain.vendas.Pedido;
import com.gakki.store.dto.response.CupomResponse;
import com.gakki.store.dto.response.ItemPedidoResponse;
import com.gakki.store.dto.response.PagamentoCartaoResponse;
import com.gakki.store.dto.response.PedidoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class PedidoMapper {

    private final CupomMapper cupomMapper;

    // Item do carrinho vira item do pedido COPIANDO nome e valor.
    //
    // É a fotografia da compra: a partir daqui o item não depende mais do
    // cadastro do instrumento. Renomear ou reprecificar o produto amanhã
    // não reescreve a nota de hoje.
    public ItemPedido paraItemPedido(ItemCarrinho item) {
        ItemPedido itemPedido = new ItemPedido();
        itemPedido.setInstrumento(item.getInstrumento());
        itemPedido.setInstrumentoNome(item.getInstrumento().getNome());
        itemPedido.setQuantidade(item.getQuantidade());
        itemPedido.setValorUnitario(item.getValorUnitario());
        return itemPedido;
    }

    public Pagamento paraPagamento(Cartao cartao, BigDecimal valor) {
        Pagamento pagamento = new Pagamento();
        pagamento.setCartao(cartao);
        pagamento.setValor(valor);
        return pagamento;
    }

    // Linha de cupom guarda o valor DE FACE do cupom, não o quanto dele
    // foi aproveitado. Quando sobra, a diferença volta como cupom de
    // troco — igual a pagar em dinheiro e receber troco. Por isso a soma
    // das linhas de pagamento pode passar do valor_total: ela é sempre
    // valor_total + troco.
    public Pagamento paraPagamento(Cupom cupom) {
        Pagamento pagamento = new Pagamento();
        pagamento.setCupom(cupom);
        pagamento.setValor(cupom.getValor());
        return pagamento;
    }

    public PedidoResponse paraResponse(Pedido pedido) {
        List<ItemPedidoResponse> itens = pedido.getItens().stream()
                .map(this::paraResponse)
                .toList();

        List<PagamentoCartaoResponse> cartoes = pedido.getPagamentos().stream()
                .filter(pagamento -> pagamento.getCartao() != null)
                .map(pagamento -> new PagamentoCartaoResponse(
                        pagamento.getCartao().getApelido(),
                        pagamento.getCartao().getUltimosDigitos(),
                        pagamento.getValor()))
                .toList();

        List<String> cupons = pedido.getPagamentos().stream()
                .filter(pagamento -> pagamento.getCupom() != null)
                .map(pagamento -> pagamento.getCupom().getCodigo())
                .toList();

        return new PedidoResponse(
                pedido.getId(),
                pedido.getNumero(),
                pedido.getStatus().name(),
                pedido.getCliente().getNome(),
                pedido.getCliente().getUsuario().getEmail(),
                resumoDe(pedido.getEnderecoEntrega()),
                pedido.getValorFrete(),
                pedido.getValorTotal(),
                pedido.getCriadoEm(),
                itens,
                cartoes,
                cupons,
                null);
    }

    // Variante usada na finalização, que é o único momento em que existe
    // cupom de troco para informar.
    public PedidoResponse paraResponse(Pedido pedido, Cupom cupomTroco) {
        PedidoResponse resposta = paraResponse(pedido);
        return new PedidoResponse(
                resposta.id(), resposta.numero(), resposta.status(),
                resposta.clienteNome(), resposta.clienteEmail(), resposta.enderecoResumo(),
                resposta.valorFrete(), resposta.valorTotal(), resposta.criadoEm(),
                resposta.itens(), resposta.pagamentosCartao(), resposta.cuponsUtilizados(),
                cupomTroco == null ? null : cupomMapper.paraResponse(cupomTroco));
    }

    public ItemPedidoResponse paraResponse(ItemPedido item) {
        return new ItemPedidoResponse(
                item.getId(),
                item.getInstrumento().getId(),
                item.getInstrumentoNome(),
                item.getQuantidade(),
                item.getValorUnitario(),
                item.isEmTroca());
    }

    // "Rua das Guitarras, 123 — São Paulo/SP".
    //
    // O tipo de logradouro entra porque no nosso schema ele é coluna
    // própria: `logradouro` guarda só "das Guitarras". O mock do front
    // não tinha essa separação e montava o resumo sem o tipo.
    private String resumoDe(Endereco endereco) {
        return "%s %s, %s — %s/%s".formatted(
                capitalizar(endereco.getTipoLogradouro().name()),
                endereco.getLogradouro(),
                endereco.getNumero(),
                endereco.getCidade(),
                endereco.getEstado());
    }

    private String capitalizar(String texto) {
        return texto.charAt(0) + texto.substring(1).toLowerCase(Locale.ROOT);
    }
}
