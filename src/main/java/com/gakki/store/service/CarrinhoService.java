package com.gakki.store.service;

import com.gakki.store.domain.cliente.Cliente;
import com.gakki.store.domain.cliente.Endereco;
import com.gakki.store.domain.vendas.Carrinho;
import com.gakki.store.domain.vendas.Instrumento;
import com.gakki.store.domain.vendas.ItemCarrinho;
import com.gakki.store.dto.request.ItemCarrinhoRequest;
import com.gakki.store.dto.response.CarrinhoResponse;
import com.gakki.store.dto.response.FreteResponse;
import com.gakki.store.exception.RecursoNaoEncontradoException;
import com.gakki.store.exception.RegraDeNegocioException;
import com.gakki.store.mapper.CarrinhoMapper;
import com.gakki.store.repository.CarrinhoRepository;
import com.gakki.store.repository.ClienteRepository;
import com.gakki.store.repository.EnderecoRepository;
import com.gakki.store.repository.InstrumentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

// RF0031, RF0032, RN0031 e RF0034 — carrinho e prévia de frete.
//
// Nada aqui decrementa estoque: a RN0031 nesta fase é comparação, não
// reserva (contrato de Vendas v3, decisão 1).

@Service
@RequiredArgsConstructor
public class CarrinhoService {

    // Fórmula do frete (RF0034). O DRS não a define — esta é nossa, e
    // precisa ser explicada na apresentação (contrato de Vendas v3,
    // decisão 10). Constantes nomeadas em vez de números soltos no meio
    // do cálculo: a banca vai perguntar de onde sai cada parcela.
    private static final BigDecimal FRETE_BASE = new BigDecimal("15.00");
    private static final BigDecimal FRETE_POR_PECA_ADICIONAL = new BigDecimal("5.00");
    private static final BigDecimal FRETE_ADICIONAL_FORA_DE_SP = new BigDecimal("10.00");
    private static final String ESTADO_SEM_ADICIONAL = "SP";

    private final CarrinhoRepository carrinhoRepository;
    private final InstrumentoRepository instrumentoRepository;
    private final EnderecoRepository enderecoRepository;
    private final ClienteRepository clienteRepository;
    private final CarrinhoMapper carrinhoMapper;

    // RF0033 — "iniciar a compra" é navegação de tela; o backend não
    // distingue ver o carrinho de abrir o checkout.
    @Transactional(readOnly = true)
    public CarrinhoResponse verCarrinho(String email) {
        return carrinhoRepository.buscarComItens(clienteDoEmail(email).getId())
                .map(carrinhoMapper::paraResponse)
                .orElseGet(CarrinhoResponse::vazio);
    }

    // RF0031 — adicionar item, com a quantidade escolhida já na adição.
    @Transactional
    public CarrinhoResponse adicionar(String email, ItemCarrinhoRequest requisicao) {
        Cliente cliente = clienteDoEmail(email);
        Instrumento instrumento = instrumentoAtivo(requisicao.instrumentoId());
        Carrinho carrinho = carrinhoDoCliente(cliente);

        // O índice ux_item_carrinho_instrumento não admite duas linhas do
        // mesmo instrumento: adicionar de novo soma na linha existente.
        ItemCarrinho existente = itemDoInstrumento(carrinho, instrumento.getId());
        int quantidadeFinal = requisicao.quantidade()
                + (existente == null ? 0 : existente.getQuantidade());

        // A validação é sobre o total que ficará no carrinho, não sobre o
        // que está sendo somado agora: três adições de 2 unidades em um
        // estoque de 5 têm de falhar na terceira.
        validarDisponibilidade(instrumento, quantidadeFinal);

        if (existente == null) {
            carrinho.adicionarItem(carrinhoMapper.novoItem(instrumento, requisicao.quantidade()));
        } else {
            existente.setQuantidade(quantidadeFinal);
        }

        // saveAndFlush para o item novo receber o id antes de virar DTO —
        // sem isso o `itemId` da resposta sairia nulo e a tela não teria
        // como chamar alterar nem remover.
        return carrinhoMapper.paraResponse(carrinhoRepository.saveAndFlush(carrinho));
    }

    // RF0032 — alterar a quantidade na visualização do carrinho.
    @Transactional
    public CarrinhoResponse atualizarQuantidade(String email, Long itemId, Integer quantidade) {
        Carrinho carrinho = carrinhoComItens(clienteDoEmail(email).getId());
        ItemCarrinho item = itemDoCarrinho(carrinho, itemId);

        validarDisponibilidade(item.getInstrumento(), quantidade);
        item.setQuantidade(quantidade);

        return carrinhoMapper.paraResponse(carrinho);
    }

    // RF0032 — excluir item do carrinho.
    @Transactional
    public void remover(String email, Long itemId) {
        Carrinho carrinho = carrinhoComItens(clienteDoEmail(email).getId());
        ItemCarrinho item = itemDoCarrinho(carrinho, itemId);

        // Remover da coleção basta: orphanRemoval apaga a linha. Um
        // delete direto pelo repository deixaria a coleção em memória
        // dessincronizada do banco dentro da mesma transação.
        carrinho.removerItem(item);
    }

    // RF0034 — prévia do frete para o endereço escolhido no checkout.
    @Transactional(readOnly = true)
    public FreteResponse calcularFrete(String email, Long enderecoEntregaId) {
        Cliente cliente = clienteDoEmail(email);

        Endereco endereco = enderecoRepository
                .findByIdAndClienteId(enderecoEntregaId, cliente.getId())
                .orElseThrow(() -> RecursoNaoEncontradoException.endereco(enderecoEntregaId));

        Carrinho carrinho = carrinhoComItens(cliente.getId());
        if (carrinho.getItens().isEmpty()) {
            throw new RegraDeNegocioException("Não há itens no carrinho para calcular o frete.");
        }

        BigDecimal frete = calcularFrete(carrinho, endereco);
        return new FreteResponse(frete, carrinhoMapper.totalDe(carrinho).add(frete));
    }

    /*
     * Fórmula do frete, em um lugar só.
     *
     * Base + adicional por peça além da primeira + adicional para fora de
     * SP. "Peça" é a soma das quantidades, não o número de linhas: dois
     * pedais iguais ocupam a mesma caixa que dois pedais diferentes.
     *
     * Público porque o PedidoService recalcula o frete na finalização em
     * vez de aceitar o que o front mandou (decisão 14). Duas fórmulas
     * acabariam divergindo; esta é a única.
     */
    public BigDecimal calcularFrete(Carrinho carrinho, Endereco endereco) {
        int pecas = carrinho.getItens().stream()
                .mapToInt(ItemCarrinho::getQuantidade)
                .sum();

        BigDecimal frete = FRETE_BASE.add(
                FRETE_POR_PECA_ADICIONAL.multiply(BigDecimal.valueOf(Math.max(0, pecas - 1))));

        if (!ESTADO_SEM_ADICIONAL.equalsIgnoreCase(endereco.getEstado())) {
            frete = frete.add(FRETE_ADICIONAL_FORA_DE_SP);
        }

        return frete.setScale(2, RoundingMode.HALF_UP);
    }

    // Usado pelo PedidoService: o carrinho que vira pedido é o mesmo que
    // a tela mostrou, carregado com os itens numa consulta só.
    @Transactional(readOnly = true)
    public Carrinho carrinhoComItens(Long clienteId) {
        return carrinhoRepository.buscarComItens(clienteId)
                .orElseThrow(() -> new RegraDeNegocioException("O carrinho está vazio."));
    }

    // RN0031 — não é permitido pedir mais do que existe em estoque.
    private void validarDisponibilidade(Instrumento instrumento, int quantidade) {
        if (instrumento.getQuantidadeEstoque() < quantidade) {
            throw new RegraDeNegocioException(
                    "Estoque insuficiente para %s: disponível %d."
                            .formatted(instrumento.getNome(), instrumento.getQuantidadeEstoque()));
        }
    }

    // Carrinho criado sob demanda: não existe linha de carrinho vazio
    // esperando no banco para cliente que nunca comprou (decisão 11).
    private Carrinho carrinhoDoCliente(Cliente cliente) {
        return carrinhoRepository.buscarComItens(cliente.getId())
                .orElseGet(() -> {
                    Carrinho novo = new Carrinho();
                    novo.setCliente(cliente);
                    return novo;
                });
    }

    private ItemCarrinho itemDoInstrumento(Carrinho carrinho, Long instrumentoId) {
        return carrinho.getItens().stream()
                // O instrumento vem carregado pelo JOIN FETCH do
                // buscarComItens, então getId() aqui não inicializa proxy
                // nem dispara consulta.
                .filter(item -> item.getInstrumento().getId().equals(instrumentoId))
                .findFirst()
                .orElse(null);
    }

    private ItemCarrinho itemDoCarrinho(Carrinho carrinho, Long itemId) {
        return carrinho.getItens().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                // Procurar dentro do carrinho do cliente, e não pelo id
                // solto, é o que impede alterar item do carrinho alheio.
                .orElseThrow(() -> RecursoNaoEncontradoException.itemDeCarrinho(itemId));
    }

    private Instrumento instrumentoAtivo(Long instrumentoId) {
        return instrumentoRepository.findByIdAndAtivoTrue(instrumentoId)
                .orElseThrow(() -> RecursoNaoEncontradoException.instrumento(instrumentoId));
    }

    private Cliente clienteDoEmail(String email) {
        return clienteRepository.findByUsuarioEmail(email)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Nenhum cadastro de cliente associado a este usuário."));
    }
}
