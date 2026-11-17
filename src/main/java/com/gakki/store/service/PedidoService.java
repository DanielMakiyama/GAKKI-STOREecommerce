package com.gakki.store.service;

import com.gakki.store.domain.cliente.Cartao;
import com.gakki.store.domain.cliente.Cliente;
import com.gakki.store.domain.cliente.Endereco;
import com.gakki.store.domain.vendas.Carrinho;
import com.gakki.store.domain.vendas.Cupom;
import com.gakki.store.domain.vendas.Instrumento;
import com.gakki.store.domain.vendas.ItemPedido;
import com.gakki.store.domain.vendas.Pedido;
import com.gakki.store.domain.vendas.enums.StatusPedido;
import com.gakki.store.dto.request.FinalizarCompraRequest;
import com.gakki.store.dto.request.PagamentoCartaoRequest;
import com.gakki.store.dto.response.PaginaResponse;
import com.gakki.store.dto.response.PedidoResponse;
import com.gakki.store.exception.ConflitoException;
import com.gakki.store.exception.RecursoNaoEncontradoException;
import com.gakki.store.exception.RegraDeNegocioException;
import com.gakki.store.mapper.CarrinhoMapper;
import com.gakki.store.mapper.PedidoMapper;
import com.gakki.store.repository.CartaoRepository;
import com.gakki.store.repository.ClienteRepository;
import com.gakki.store.repository.EnderecoRepository;
import com.gakki.store.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

// Ciclo de vida da venda, do carrinho à entrega.

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
    private final OperadoraDeCartao operadora;

    // Uma transação só, do começo ao fim (RF0038).

    @Transactional
    public PedidoResponse finalizarCompra(String email, FinalizarCompraRequest requisicao) {
        Cliente cliente = clienteDoEmail(email);
        Endereco endereco = enderecoDeEntrega(cliente, requisicao.enderecoEntregaId());
        Carrinho carrinho = carrinhoService.carrinhoComItens(cliente.getId());

        if (carrinho.getItens().isEmpty()) {
            throw new RegraDeNegocioException("Não é possível finalizar uma compra com o carrinho vazio.");
        }

        // RNF0042 — enquanto houver item retirado por expiração, a compra fica desabilitada.
        if (!carrinhoService.itensRetirados(carrinho.getId()).isEmpty()) {
            throw new RegraDeNegocioException(
                    "Há itens retirados do carrinho por expiração do prazo. "
                            + "Devolva-os ao carrinho ou descarte-os antes de comprar.");
        }

        // RN0031 conferida de novo agora, e não só na adição: entre pôr
        carrinho.getItens().forEach(item ->
                carrinhoService.validarDisponibilidade(
                        item.getInstrumento(), item.getQuantidade(), cliente.getId()));

        // Valores calculados aqui, nunca lidos da requisição
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

        // RN0044 — daqui para a frente o bloqueio é do pedido, não do
        // carrinho: a mercadoria segue comprometida enquanto o pedido
        // estiver EM_PROCESSAMENTO. Zerar o prazo evita que a tela mostre
        // uma contagem correndo sobre um carrinho que já não tem itens.
        carrinho.setBloqueadoAte(null);

        return pedidoMapper.paraResponse(gravado, troco);
    }

    @Transactional(readOnly = true)
    public PaginaResponse<PedidoResponse> meusPedidos(String email, Pageable paginacao) {
        Long clienteId = clienteDoEmail(email).getId();
        return PaginaResponse.de(
                pedidoRepository.findByClienteIdOrderByCriadoEmDesc(clienteId, paginacao)
                        .map(pedidoMapper::paraResponse));
    }


    // Ciclo de vida do pedido - ações do administrador
    @Transactional(readOnly = true)
    public PaginaResponse<PedidoResponse> pedidosParaAdministracao(String status, Pageable paginacao) {
        StatusPedido filtro = statusInformado(status);

        Page<Pedido> pagina = filtro == null
                ? pedidoRepository.findAllByOrderByCriadoEmDesc(paginacao)
                : pedidoRepository.findByStatusOrderByCriadoEmDesc(filtro, paginacao);

        return PaginaResponse.de(pagina.map(pedidoMapper::paraResponse));
    }

    //RN0037 e RN0038 — validação da forma de pagamento

    @Transactional
    public PedidoResponse confirmarPagamento(Long pedidoId) {
        Pedido pedido = pedidoParaAdministracao(pedidoId);
        exigirStatus(pedido, StatusPedido.EM_PROCESSAMENTO, "validar o pagamento");

        OperadoraDeCartao.Retorno retorno = operadora.autorizar(pedido);

        if (!retorno.aprovado()) {
            pedido.setStatus(StatusPedido.REPROVADA);
            return pedidoMapper.paraResponse(pedido);
        }

        pedido.setStatus(StatusPedido.APROVADA);
        darBaixaEmEstoque(pedido);
        return pedidoMapper.paraResponse(pedido);
    }

    // RF0039 e RN0039 — despacho. Só pedido APROVADA pode ser despachado

    @Transactional
    public PedidoResponse despachar(Long pedidoId) {
        Pedido pedido = pedidoParaAdministracao(pedidoId);
        exigirStatus(pedido, StatusPedido.APROVADA, "despachar o pedido");
        pedido.setStatus(StatusPedido.EM_TRANSPORTE);
        return pedidoMapper.paraResponse(pedido);
    }

    // RF0040 e RN0040 - confirmação de entrega.

    @Transactional
    public PedidoResponse confirmarEntrega(Long pedidoId) {
        Pedido pedido = pedidoParaAdministracao(pedidoId);
        exigirStatus(pedido, StatusPedido.EM_TRANSPORTE, "confirmar a entrega");
        pedido.setStatus(StatusPedido.ENTREGUE);
        return pedidoMapper.paraResponse(pedido);
    }

    // RF0053 — baixa do total de itens vendidos.

    private void darBaixaEmEstoque(Pedido pedido) {
        for (ItemPedido item : pedido.getItens()) {
            Instrumento instrumento = item.getInstrumento();
            int saldo = instrumento.getQuantidadeEstoque() - item.getQuantidade();

            if (saldo < 0) {
                throw new ConflitoException(
                        ("Estoque insuficiente para aprovar o pedido %s: %s tem %d em estoque "
                                + "e a compra pede %d.")
                                .formatted(pedido.getNumero(), instrumento.getNome(),
                                        instrumento.getQuantidadeEstoque(), item.getQuantidade()));
            }
            instrumento.setQuantidadeEstoque(saldo);
        }
    }

    private void exigirStatus(Pedido pedido, StatusPedido esperado, String acao) {
        if (pedido.getStatus() != esperado) {
            throw new ConflitoException(
                    "Não é possível %s: o pedido %s está em %s e a ação exige %s."
                            .formatted(acao, pedido.getNumero(),
                                    rotulo(pedido.getStatus()), rotulo(esperado)));
        }
    }

    private Pedido pedidoParaAdministracao(Long pedidoId) {
        return pedidoRepository.buscarComItensParaAdministracao(pedidoId)
                .orElseThrow(() -> RecursoNaoEncontradoException.pedido(pedidoId));
    }

    private StatusPedido statusInformado(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        try {
            return StatusPedido.valueOf(status.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException excecao) {
            throw new RegraDeNegocioException("Status de pedido desconhecido: %s.".formatted(status));
        }
    }

    private String rotulo(StatusPedido status) {
        return status.name().replace('_', ' ');
    }

    // RN0034 e RN0035 - as duas regras de pagamento

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
        // RF0038 — a finalização sempre grava este status. A aprovação é
        // o passo seguinte, em confirmarPagamento.
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
