package com.gakki.store.service;

import com.gakki.store.domain.cliente.Cliente;
import com.gakki.store.domain.vendas.Cupom;
import com.gakki.store.domain.vendas.Instrumento;
import com.gakki.store.domain.vendas.ItemPedido;
import com.gakki.store.domain.vendas.ItemTroca;
import com.gakki.store.domain.vendas.Pedido;
import com.gakki.store.domain.vendas.Troca;
import com.gakki.store.domain.vendas.enums.StatusPedido;
import com.gakki.store.domain.vendas.enums.StatusTroca;
import com.gakki.store.dto.request.ItemTrocaRequest;
import com.gakki.store.dto.request.SolicitarTrocaRequest;
import com.gakki.store.dto.response.PaginaResponse;
import com.gakki.store.dto.response.TrocaResponse;
import com.gakki.store.exception.ConflitoException;
import com.gakki.store.exception.RecursoNaoEncontradoException;
import com.gakki.store.exception.RegraDeNegocioException;
import com.gakki.store.mapper.TrocaMapper;
import com.gakki.store.repository.ClienteRepository;
import com.gakki.store.repository.PedidoRepository;
import com.gakki.store.repository.TrocaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;

/*
 * Fluxo de troca, da solicitação ao cupom (RF0041 a RF0045).
 *
 * Trajeto, nos passos do CDU02:
 *
 *   solicitar (P9)            -> EM_TROCA
 *   autorizar (P10)           -> TROCA_AUTORIZADA + notificação (RN0046)
 *   confirmarRecebimento (P11)-> TROCADO + reentrada opcional + cupom
 *
 * O status vive na troca. No pedido ele é espelhado apenas quando a
 * troca abrange a compra inteira — é o caso que a RN0041 destaca em
 * separado ("caso o cliente solicite a troca de toda a compra o status
 * do pedido deverá ser EM TROCA"). Numa troca parcial o pedido continua
 * ENTREGUE, porque parte da compra continua entregue de fato.
 */
@Service
@RequiredArgsConstructor
public class TrocaService {

    private final TrocaRepository trocaRepository;
    private final PedidoRepository pedidoRepository;
    private final ClienteRepository clienteRepository;
    private final CupomService cupomService;
    private final NotificacaoService notificacaoService;
    private final TrocaMapper trocaMapper;

    /*
     * RF0041 e RN0041 — o cliente seleciona itens de uma compra e o
     * sistema gera o pedido de troca.
     *
     * A RN0043 é a porta de entrada: só compra ENTREGUE pode ser
     * trocada. A checagem vem antes de qualquer outra coisa porque é a
     * que define se o fluxo existe.
     */
    @Transactional
    public TrocaResponse solicitar(String email, SolicitarTrocaRequest requisicao) {
        Cliente cliente = clienteDoEmail(email);

        // O dono entra na busca: um pedido de outra pessoa simplesmente
        // não é encontrado, e a resposta é 404 em vez de 403.
        Pedido pedido = pedidoRepository
                .buscarComItens(requisicao.pedidoId(), cliente.getId())
                .orElseThrow(() -> RecursoNaoEncontradoException.pedido(requisicao.pedidoId()));

        if (pedido.getStatus() != StatusPedido.ENTREGUE) {
            throw new ConflitoException(
                    ("Somente itens de compras entregues podem ser trocados: o pedido %s está em %s "
                            + "(RN0043).").formatted(pedido.getNumero(), rotulo(pedido.getStatus())));
        }

        exigirItensDistintos(requisicao.itens());

        Troca troca = new Troca();
        troca.setNumero(proximoNumero());
        troca.setPedido(pedido);
        troca.setCliente(cliente);
        troca.setStatus(StatusTroca.EM_TROCA);

        BigDecimal valor = BigDecimal.ZERO;
        for (ItemTrocaRequest linha : requisicao.itens()) {
            ItemPedido itemPedido = itemDoPedido(pedido, linha.itemPedidoId());

            // Um item já em troca não pode entrar noutra: a coluna
            // em_troca é o que impede a mesma unidade de gerar dois
            // cupons.
            if (itemPedido.isEmTroca()) {
                throw new ConflitoException(
                        "O item %s já faz parte de um pedido de troca."
                                .formatted(itemPedido.getInstrumentoNome()));
            }
            if (linha.quantidade() > itemPedido.getQuantidade()) {
                throw new RegraDeNegocioException(
                        "Não é possível trocar %d unidades de %s: a compra tem %d."
                                .formatted(linha.quantidade(), itemPedido.getInstrumentoNome(),
                                        itemPedido.getQuantidade()));
            }

            itemPedido.setEmTroca(true);

            ItemTroca itemTroca = new ItemTroca();
            itemTroca.setItemPedido(itemPedido);
            itemTroca.setQuantidade(linha.quantidade());
            troca.adicionarItem(itemTroca);

            // Valor pelo preço da compra, não pelo preço de hoje: é o que
            // o cliente pagou, e é o que o cupom da RF0045 vai valer.
            valor = valor.add(itemPedido.getValorUnitario()
                    .multiply(BigDecimal.valueOf(linha.quantidade())));
        }
        troca.setValor(valor);

        // RN0041 — a compra inteira em troca espelha o status no pedido.
        if (pedido.getItens().stream().allMatch(ItemPedido::isEmTroca)) {
            pedido.setStatus(StatusPedido.EM_TROCA);
        }

        return trocaMapper.paraResponse(trocaRepository.save(troca));
    }

    @Transactional(readOnly = true)
    public PaginaResponse<TrocaResponse> minhasTrocas(String email, Pageable paginacao) {
        Long clienteId = clienteDoEmail(email).getId();
        return PaginaResponse.de(
                trocaRepository.findByClienteIdOrderByCriadoEmDesc(clienteId, paginacao)
                        .map(trocaMapper::paraResponse));
    }

    // RF0043 — todas as trocas, com filtro de status opcional.
    @Transactional(readOnly = true)
    public PaginaResponse<TrocaResponse> trocasParaAdministracao(String status, Pageable paginacao) {
        StatusTroca filtro = statusInformado(status);

        Page<Troca> pagina = filtro == null
                ? trocaRepository.findAllByOrderByCriadoEmDesc(paginacao)
                : trocaRepository.findByStatusOrderByCriadoEmDesc(filtro, paginacao);

        return PaginaResponse.de(pagina.map(trocaMapper::paraResponse));
    }

    /*
     * RF0042 e RN0046 — autorização e notificação ao cliente.
     *
     * A notificação sai na mesma transação. Se a gravação falhar depois,
     * o cliente não pode ficar avisado de uma autorização que não
     * aconteceu.
     */
    @Transactional
    public TrocaResponse autorizar(Long trocaId) {
        Troca troca = trocaComItens(trocaId);
        exigirStatus(troca, StatusTroca.EM_TROCA, "autorizar a troca");

        troca.setStatus(StatusTroca.TROCA_AUTORIZADA);
        troca.setAtualizadoEm(OffsetDateTime.now());
        espelharNoPedido(troca, StatusPedido.EM_TROCA, StatusPedido.TROCA_AUTORIZADA);

        notificacaoService.notificar(troca.getCliente(),
                "Sua solicitação de troca %s foi autorizada. Envie os itens para análise."
                        .formatted(troca.getNumero()));

        return trocaMapper.paraResponse(troca);
    }

    /*
     * RF0044, RF0045, RF0054 e RN0042 — o administrador confirma que os
     * itens chegaram.
     *
     * Três efeitos numa transação só: a reentrada em estoque quando
     * indicada, o cupom de troca emitido ao cliente e o status final.
     * Emitir o cupom sem gravar o status deixaria o cliente com dinheiro
     * e a troca eternamente em aberto.
     */
    @Transactional
    public TrocaResponse confirmarRecebimento(Long trocaId, boolean retornaEstoque) {
        Troca troca = trocaComItens(trocaId);
        exigirStatus(troca, StatusTroca.TROCA_AUTORIZADA, "confirmar o recebimento");

        troca.setRetornaEstoque(retornaEstoque);

        if (retornaEstoque) {
            darEntradaEmEstoque(troca);
        }

        // RF0045 — o cupom vale o que os itens trocados valiam na compra.
        Cupom cupom = cupomService.emitirCupomDeTroca(troca.getCliente(), troca.getValor());
        troca.setCupom(cupom);

        troca.setStatus(StatusTroca.TROCADO);
        troca.setAtualizadoEm(OffsetDateTime.now());
        espelharNoPedido(troca, StatusPedido.TROCA_AUTORIZADA, StatusPedido.TROCADO);

        return trocaMapper.paraResponse(troca);
    }

    /*
     * RF0054 — reentrada dos itens trocados.
     *
     * Soma na quantidade do instrumento, que é onde vive o saldo. Não
     * cria linha em item_estoque de propósito: a RN0050 exige fornecedor,
     * valor de custo e data de entrada para cada registro, e uma
     * devolução não traz nenhum dos três. Inventar um fornecedor para
     * caber na tabela seria pior que não registrar — e a RNF0012 já
     * guarda quem fez, quando e o que mudou.
     */
    private void darEntradaEmEstoque(Troca troca) {
        for (ItemTroca item : troca.getItens()) {
            Instrumento instrumento = item.getItemPedido().getInstrumento();
            instrumento.setQuantidadeEstoque(
                    instrumento.getQuantidadeEstoque() + item.getQuantidade());
        }
    }

    /*
     * RN0041 e RN0042 falam do status "do pedido ou da compra". O pedido
     * só acompanha a troca quando ela cobre a compra inteira; numa troca
     * parcial ele continua ENTREGUE, porque parte da compra continua
     * entregue de fato.
     */
    private void espelharNoPedido(Troca troca, StatusPedido de, StatusPedido para) {
        if (troca.getPedido().getStatus() == de) {
            troca.getPedido().setStatus(para);
        }
    }

    private void exigirItensDistintos(List<ItemTrocaRequest> itens) {
        long distintos = itens.stream().map(ItemTrocaRequest::itemPedidoId).distinct().count();
        if (distintos != itens.size()) {
            throw new RegraDeNegocioException("O mesmo item foi informado mais de uma vez.");
        }
    }

    private ItemPedido itemDoPedido(Pedido pedido, Long itemPedidoId) {
        return pedido.getItens().stream()
                .filter(item -> item.getId().equals(itemPedidoId))
                .findFirst()
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "O item %d não pertence ao pedido %s."
                                .formatted(itemPedidoId, pedido.getNumero())));
    }

    private Troca trocaComItens(Long trocaId) {
        return trocaRepository.buscarComItens(trocaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Pedido de troca %d não encontrado.".formatted(trocaId)));
    }

    private void exigirStatus(Troca troca, StatusTroca esperado, String acao) {
        if (troca.getStatus() != esperado) {
            throw new ConflitoException(
                    "Não é possível %s: a troca %s está em %s e a ação exige %s."
                            .formatted(acao, troca.getNumero(),
                                    rotulo(troca.getStatus()), rotulo(esperado)));
        }
    }

    private StatusTroca statusInformado(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        try {
            return StatusTroca.valueOf(status.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException excecao) {
            throw new RegraDeNegocioException("Status de troca desconhecido: %s.".formatted(status));
        }
    }

    private String proximoNumero() {
        return "TRC-%05d".formatted(trocaRepository.proximoNumeroDeTroca());
    }

    private String rotulo(Enum<?> status) {
        return status.name().replace('_', ' ');
    }

    private Cliente clienteDoEmail(String email) {
        return clienteRepository.findByUsuarioEmail(email)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Nenhum cadastro de cliente associado a este usuário."));
    }
}
