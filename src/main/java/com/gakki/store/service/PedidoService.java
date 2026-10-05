package com.gakki.store.service;

import com.gakki.store.domain.cliente.Cartao;
import com.gakki.store.domain.cliente.Cliente;
import com.gakki.store.domain.cliente.Endereco;
import com.gakki.store.domain.vendas.Carrinho;
import com.gakki.store.domain.vendas.Cupom;
import com.gakki.store.domain.vendas.ItemCarrinho;
import com.gakki.store.domain.vendas.Pedido;
import com.gakki.store.domain.vendas.enums.StatusPedido;
import com.gakki.store.dto.request.FinalizarCompraRequest;
import com.gakki.store.dto.request.PagamentoCartaoRequest;
import com.gakki.store.dto.response.PaginaResponse;
import com.gakki.store.dto.response.PedidoResponse;
import com.gakki.store.exception.RecursoNaoEncontradoException;
import com.gakki.store.exception.RegraDeNegocioException;
import com.gakki.store.mapper.CarrinhoMapper;
import com.gakki.store.mapper.PedidoMapper;
import com.gakki.store.repository.CartaoRepository;
import com.gakki.store.repository.ClienteRepository;
import com.gakki.store.repository.EnderecoRepository;
import com.gakki.store.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/*
 * RF0038 — finalização da compra.
 *
 * É onde cupom e cartão se encontram, e por isso é aqui que moram a
 * RN0034 (piso de R$10 por cartão) e a RN0035 (o piso cai quando há
 * cupom): nenhuma das duas se decide olhando uma linha de pagamento
 * isolada.
 *
 * Nada aqui decrementa estoque: a baixa (RF0053, RN0028) está fora do
 * escopo desta fatia, e o status gravado é sempre EM_PROCESSAMENTO,
 * porque a validação de pagamento (RN0037, RN0038) também está
 * (contrato de Vendas v3, decisões 1 e 2).
 */
@Service
@RequiredArgsConstructor
public class PedidoService {

    // RN0034 — valor mínimo por cartão, quando não há cupom na compra.
    private static final BigDecimal MINIMO_POR_CARTAO = new BigDecimal("10.00");

    private final PedidoRepository pedidoRepository;
    private final CartaoRepository cartaoRepository;
    private final EnderecoRepository enderecoRepository;
    private final ClienteRepository clienteRepository;
    private final CarrinhoService carrinhoService;
    private final CupomService cupomService;
    private final CarrinhoMapper carrinhoMapper;
    private final PedidoMapper pedidoMapper;

    /*
     * Uma transação só, do começo ao fim (RF0038).
     *
     * Grava o pedido, os itens, as linhas de pagamento, marca os cupons
     * como usados, emite o cupom de troco e esvazia o carrinho. Qualquer
     * falha no meio desfaz tudo — o estado intermediário "cupom gasto,
     * pedido inexistente" não pode acontecer.
     */
    @Transactional
    public PedidoResponse finalizarCompra(String email, FinalizarCompraRequest requisicao) {
        Cliente cliente = clienteDoEmail(email);
        Endereco endereco = enderecoDeEntrega(cliente, requisicao.enderecoEntregaId());
        Carrinho carrinho = carrinhoService.carrinhoComItens(cliente.getId());

        if (carrinho.getItens().isEmpty()) {
            throw new RegraDeNegocioException("Não é possível finalizar uma compra com o carrinho vazio.");
        }

        // RN0031 conferida de novo agora, e não só na adição: entre pôr
        // no carrinho e fechar a compra pode ter passado tempo.
        carrinho.getItens().forEach(this::validarDisponibilidade);

        // Valores calculados aqui, nunca lidos da requisição (decisão 14).
        BigDecimal valorItens = carrinhoMapper.totalDe(carrinho);
        BigDecimal valorFrete = carrinhoService.calcularFrete(carrinho, endereco);
        BigDecimal valorTotal = valorItens.add(valorFrete);

        // RN0033 e RN0036 ficam no CupomService; o que volta já está válido.
        List<Cupom> cupons = cupomService.validarSelecao(cliente, requisicao.codigosCupom(), valorTotal);
        BigDecimal valorEmCupons = cupomService.somaDe(cupons);

        List<PagamentoCartaoRequest> linhasDeCartao = linhasDeCartao(requisicao);
        validarPagamento(cliente, linhasDeCartao, cupons, valorEmCupons, valorTotal);

        Pedido pedido = montarPedido(cliente, endereco, carrinho, valorFrete, valorTotal);
        cupons.forEach(cupom -> pedido.adicionarPagamento(pedidoMapper.paraPagamento(cupom)));
        linhasDeCartao.forEach(linha -> pedido.adicionarPagamento(
                pedidoMapper.paraPagamento(cartaoDoCliente(linha.cartaoCreditoId(), cliente), linha.valor())));

        // Salva antes de amarrar os cupons: eles precisam do id do pedido.
        Pedido gravado = pedidoRepository.save(pedido);

        cupomService.marcarComoUtilizados(cupons, gravado);
        Cupom troco = cupomService
                .gerarTrocoSeNecessario(cliente, valorEmCupons, valorTotal)
                .orElse(null);

        // orphanRemoval apaga as linhas: o carrinho fica vazio, não some.
        carrinho.getItens().clear();

        return pedidoMapper.paraResponse(gravado, troco);
    }

    @Transactional(readOnly = true)
    public PaginaResponse<PedidoResponse> meusPedidos(String email, Pageable paginacao) {
        Long clienteId = clienteDoEmail(email).getId();
        return PaginaResponse.de(
                pedidoRepository.findByClienteIdOrderByCriadoEmDesc(clienteId, paginacao)
                        .map(pedidoMapper::paraResponse));
    }

    /*
     * RN0034 e RN0035 — as duas regras de pagamento, juntas porque são a
     * mesma decisão vista de dois ângulos.
     *
     * Sem cupom: a soma dos cartões tem de fechar o total exato, e cada
     * linha precisa de pelo menos R$10 (RN0034).
     *
     * Com cupom: o valor em cupons é sempre aproveitado por inteiro — é o
     * que a RN0035 chama de "considerar sempre o valor máximo dos cupons".
     * Se ainda faltar, o cartão cobre a diferença SEM piso de R$10, que é
     * a exceção que a própria RN0035 abre. Se os cupons já cobrem tudo,
     * cartão nenhum pode entrar: seria cobrar por algo já pago.
     */
    private void validarPagamento(Cliente cliente,
                                  List<PagamentoCartaoRequest> linhas,
                                  List<Cupom> cupons,
                                  BigDecimal valorEmCupons,
                                  BigDecimal valorTotal) {

        if (linhas.isEmpty() && cupons.isEmpty()) {
            throw new RegraDeNegocioException("Informe ao menos uma forma de pagamento.");
        }

        // Duas linhas do mesmo cartão é quase sempre engano de quem
        // preencheu — e o resultado seria uma cobrança dobrada silenciosa.
        if (linhas.stream().map(PagamentoCartaoRequest::cartaoCreditoId).distinct().count() != linhas.size()) {
            throw new RegraDeNegocioException("O mesmo cartão foi informado mais de uma vez.");
        }

        // Cada cartão precisa ser do cliente — a busca já faz a checagem.
        linhas.forEach(linha -> cartaoDoCliente(linha.cartaoCreditoId(), cliente));

        BigDecimal valorEmCartoes = linhas.stream()
                .map(PagamentoCartaoRequest::valor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal restante = valorTotal.subtract(valorEmCupons);

        if (restante.compareTo(BigDecimal.ZERO) <= 0) {
            if (!linhas.isEmpty()) {
                throw new RegraDeNegocioException(
                        "Os cupons já cobrem o total da compra; não é necessário informar cartão.");
            }
            return;
        }

        if (valorEmCartoes.compareTo(restante) != 0) {
            throw new RegraDeNegocioException(
                    ("Os pagamentos em cartão precisam somar exatamente R$ %s "
                            + "(total R$ %s menos R$ %s em cupons).")
                            .formatted(restante, valorTotal, valorEmCupons));
        }

        // RN0034, com a exceção da RN0035: o piso só vale quando a compra
        // é paga exclusivamente em cartão.
        if (cupons.isEmpty()) {
            linhas.stream()
                    .filter(linha -> linha.valor().compareTo(MINIMO_POR_CARTAO) < 0)
                    .findFirst()
                    .ifPresent(linha -> {
                        throw new RegraDeNegocioException(
                                "Cada cartão precisa receber ao menos R$ 10,00 quando não há cupom na compra.");
                    });
        }
    }

    private Pedido montarPedido(Cliente cliente,
                                Endereco endereco,
                                Carrinho carrinho,
                                BigDecimal valorFrete,
                                BigDecimal valorTotal) {

        Pedido pedido = new Pedido();
        pedido.setNumero(proximoNumero());
        pedido.setCliente(cliente);
        pedido.setEnderecoEntrega(endereco);
        // RF0038 — o status desta fatia é sempre este. Não existe caminho
        // de aprovação aqui porque a RN0037 está fora de escopo.
        pedido.setStatus(StatusPedido.EM_PROCESSAMENTO);
        pedido.setValorFrete(valorFrete);
        pedido.setValorTotal(valorTotal);

        carrinho.getItens().forEach(item -> pedido.adicionarItem(pedidoMapper.paraItemPedido(item)));
        return pedido;
    }

    // RF0035 — o endereço precisa ser do cliente e servir para entrega.
    private Endereco enderecoDeEntrega(Cliente cliente, Long enderecoId) {
        Endereco endereco = enderecoRepository.findByIdAndClienteId(enderecoId, cliente.getId())
                .orElseThrow(() -> RecursoNaoEncontradoException.endereco(enderecoId));

        if (!endereco.isEntrega()) {
            throw new RegraDeNegocioException(
                    "O endereço escolhido não está marcado como endereço de entrega.");
        }
        return endereco;
    }

    private Cartao cartaoDoCliente(Long cartaoId, Cliente cliente) {
        return cartaoRepository.findByIdAndClienteId(cartaoId, cliente.getId())
                .orElseThrow(() -> RecursoNaoEncontradoException.cartao(cartaoId));
    }

    private void validarDisponibilidade(ItemCarrinho item) {
        if (item.getInstrumento().getQuantidadeEstoque() < item.getQuantidade()) {
            throw new RegraDeNegocioException(
                    "Estoque insuficiente para %s: disponível %d."
                            .formatted(item.getInstrumento().getNome(),
                                    item.getInstrumento().getQuantidadeEstoque()));
        }
    }

    private String proximoNumero() {
        return "PED-%05d".formatted(pedidoRepository.proximoNumeroDePedido());
    }

    private List<PagamentoCartaoRequest> linhasDeCartao(FinalizarCompraRequest requisicao) {
        return requisicao.pagamentosCartao() == null ? List.of() : requisicao.pagamentosCartao();
    }

    private Cliente clienteDoEmail(String email) {
        return clienteRepository.findByUsuarioEmail(email)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Nenhum cadastro de cliente associado a este usuário."));
    }
}
