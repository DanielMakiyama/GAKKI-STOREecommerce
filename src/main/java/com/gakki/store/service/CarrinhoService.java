package com.gakki.store.service;

import com.gakki.store.domain.cliente.Cliente;
import com.gakki.store.domain.cliente.Endereco;
import com.gakki.store.domain.vendas.Carrinho;
import com.gakki.store.domain.vendas.Instrumento;
import com.gakki.store.domain.vendas.ItemCarrinho;
import com.gakki.store.domain.vendas.ItemRetirado;
import com.gakki.store.domain.vendas.enums.StatusPedido;
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
import com.gakki.store.repository.ItemRetiradoRepository;
import com.gakki.store.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

// Carrinho, frete e bloqueio temporário (RF0031, RF0032, RF0034, RN0031, RN0044, RN0045, RNF0042).

@Service
@RequiredArgsConstructor
public class CarrinhoService {

    // Fórmula do frete (RF0034).
    private static final BigDecimal FRETE_BASE = new BigDecimal("15.00");
    private static final BigDecimal FRETE_POR_PECA_ADICIONAL = new BigDecimal("5.00");
    private static final BigDecimal FRETE_ADICIONAL_FORA_DE_SP = new BigDecimal("10.00");
    private static final String ESTADO_SEM_ADICIONAL = "SP";
    private final CarrinhoRepository carrinhoRepository;
    private final InstrumentoRepository instrumentoRepository;
    private final EnderecoRepository enderecoRepository;
    private final ClienteRepository clienteRepository;
    private final PedidoRepository pedidoRepository;
    private final ItemRetiradoRepository itemRetiradoRepository;
    private final CarrinhoMapper carrinhoMapper;

    // RN0044 - prazo parametrizado.
    @Value("${gakki.carrinho.prazo-bloqueio:PT15M}")
    private Duration prazoBloqueio;

    // RF0033 - "iniciar a compra" é navegação de tela
    @Transactional
    public CarrinhoResponse verCarrinho(String email) {
        Cliente cliente = clienteDoEmail(email);

        return carrinhoRepository.buscarComItens(cliente.getId())
                .map(carrinho -> {
                    aplicarExpiracao(carrinho);
                    return responder(carrinho);
                })
                .orElseGet(CarrinhoResponse::vazio);
    }

    // RF0031 - adicionar item, com a quantidade escolhida já na adição.
    @Transactional
    public CarrinhoResponse adicionar(String email, ItemCarrinhoRequest requisicao) {
        Cliente cliente = clienteDoEmail(email);
        Instrumento instrumento = instrumentoAtivo(requisicao.instrumentoId());
        Carrinho carrinho = carrinhoDoCliente(cliente);
        aplicarExpiracao(carrinho);

        ItemCarrinho existente = itemDoInstrumento(carrinho, instrumento.getId());
        int quantidadeFinal = requisicao.quantidade()
                + (existente == null ? 0 : existente.getQuantidade());

        validarDisponibilidade(instrumento, quantidadeFinal, cliente.getId());

        if (existente == null) {
            carrinho.adicionarItem(carrinhoMapper.novoItem(instrumento, requisicao.quantidade()));
        } else {
            existente.setQuantidade(quantidadeFinal);
        }

        // RN0044 - o prazo é relativo ao último item incluído.
        renovarBloqueio(carrinho);

        return responder(carrinhoRepository.saveAndFlush(carrinho));
    }

    // RF0032 - alterar a quantidade na visualização do carrinho.
    @Transactional
    public CarrinhoResponse atualizarQuantidade(String email, Long itemId, Integer quantidade) {
        Cliente cliente = clienteDoEmail(email);
        Carrinho carrinho = carrinhoComItens(cliente.getId());
        aplicarExpiracao(carrinho);

        ItemCarrinho item = itemDoCarrinho(carrinho, itemId);
        validarDisponibilidade(item.getInstrumento(), quantidade, cliente.getId());
        item.setQuantidade(quantidade);

        renovarBloqueio(carrinho);
        return responder(carrinho);
    }

    // RF0032 - excluir item do carrinho.
    @Transactional
    public void remover(String email, Long itemId) {
        Carrinho carrinho = carrinhoComItens(clienteDoEmail(email).getId());
        ItemCarrinho item = itemDoCarrinho(carrinho, itemId);
        carrinho.removerItem(item);
    }

    // RNF0042 - "os itens deverão ser adicionados novamente no carrinho".

    @Transactional
    public CarrinhoResponse restaurar(String email, Long itemRetiradoId) {
        Cliente cliente = clienteDoEmail(email);
        Carrinho carrinho = carrinhoComItens(cliente.getId());

        ItemRetirado retirado = itemRetiradoRepository
                .findByIdAndCarrinhoId(itemRetiradoId, carrinho.getId())
                .orElseThrow(() -> RecursoNaoEncontradoException.itemDeCarrinho(itemRetiradoId));

        Instrumento instrumento = retirado.getInstrumento();
        ItemCarrinho existente = itemDoInstrumento(carrinho, instrumento.getId());
        int quantidadeFinal = retirado.getQuantidade()
                + (existente == null ? 0 : existente.getQuantidade());

        validarDisponibilidade(instrumento, quantidadeFinal, cliente.getId());

        if (existente == null) {
            carrinho.adicionarItem(carrinhoMapper.novoItem(instrumento, retirado.getQuantidade()));
        } else {
            existente.setQuantidade(quantidadeFinal);
        }

        itemRetiradoRepository.delete(retirado);
        renovarBloqueio(carrinho);

        return responder(carrinhoRepository.saveAndFlush(carrinho));
    }

    // O outro caminho da RNF0042: desistir do item retirado.

    @Transactional
    public CarrinhoResponse descartar(String email, Long itemRetiradoId) {
        Carrinho carrinho = carrinhoComItens(clienteDoEmail(email).getId());

        ItemRetirado retirado = itemRetiradoRepository
                .findByIdAndCarrinhoId(itemRetiradoId, carrinho.getId())
                .orElseThrow(() -> RecursoNaoEncontradoException.itemDeCarrinho(itemRetiradoId));

        itemRetiradoRepository.delete(retirado);
        itemRetiradoRepository.flush();

        return responder(carrinho);
    }

    @Transactional
    public CarrinhoResponse descartarTodos(String email) {
        Cliente cliente = clienteDoEmail(email);

        return carrinhoRepository.buscarComItens(cliente.getId())
                .map(carrinho -> {
                    itemRetiradoRepository.deleteByCarrinhoId(carrinho.getId());
                    itemRetiradoRepository.flush();
                    return responder(carrinho);
                })
                .orElseGet(CarrinhoResponse::vazio);
    }

    // RF0034 - prévia do frete para o endereço escolhido no checkout.
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

    // Fórmula do frete, em um lugar só.
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

    @Transactional(readOnly = true)
    public Carrinho carrinhoComItens(Long clienteId) {
        return carrinhoRepository.buscarComItens(clienteId)
                .orElseThrow(() -> new RegraDeNegocioException("O carrinho está vazio."));
    }

    // RNF0042 - a finalização consulta isto para recusar enquanto houver item retirado esperando decisão do cliente.
    @Transactional(readOnly = true)
    public List<ItemRetirado> itensRetirados(Long carrinhoId) {
        return carrinhoId == null ? List.of() : itemRetiradoRepository.doCarrinho(carrinhoId);
    }

    // RN0031 e RN0044 — o disponível não é o estoque: é o estoque menos o que está bloqueado.

    public int disponivelPara(Instrumento instrumento, Long clienteId) {
        long emCarrinhosAlheios = valor(carrinhoRepository.quantidadeBloqueadaPorOutros(
                instrumento.getId(), clienteId, OffsetDateTime.now()));

        long aguardandoAprovacao = valor(pedidoRepository.quantidadeComStatus(
                instrumento.getId(), StatusPedido.EM_PROCESSAMENTO));

        return (int) (instrumento.getQuantidadeEstoque() - emCarrinhosAlheios - aguardandoAprovacao);
    }

    public void validarDisponibilidade(Instrumento instrumento, int quantidade, Long clienteId) {
        int disponivel = disponivelPara(instrumento, clienteId);

        if (disponivel < quantidade) {
            throw new RegraDeNegocioException(
                    "Estoque insuficiente para %s: disponível %d."
                            .formatted(instrumento.getNome(), Math.max(0, disponivel)));
        }
    }

    // RN0044 e RN0045 — vencido o prazo, TODOS os itens do carrinho saem.
    private void aplicarExpiracao(Carrinho carrinho) {
        OffsetDateTime agora = OffsetDateTime.now();

        if (carrinho.getBloqueadoAte() == null
                || carrinho.getBloqueadoAte().isAfter(agora)
                || carrinho.getItens().isEmpty()) {
            return;
        }

        List<ItemRetirado> retirados = new ArrayList<>();
        for (ItemCarrinho item : carrinho.getItens()) {
            ItemRetirado retirado = new ItemRetirado();
            retirado.setCarrinhoId(carrinho.getId());
            retirado.setInstrumento(item.getInstrumento());
            retirado.setQuantidade(item.getQuantidade());
            retirado.setValorUnitario(item.getValorUnitario());
            retirados.add(retirado);
        }
        itemRetiradoRepository.saveAll(retirados);

        // orphanRemoval apaga as linhas; o carrinho fica vazio, não some.
        carrinho.getItens().clear();
        carrinho.setBloqueadoAte(null);
    }

    private void renovarBloqueio(Carrinho carrinho) {
        carrinho.setBloqueadoAte(OffsetDateTime.now().plus(prazoBloqueio));
    }

    private CarrinhoResponse responder(Carrinho carrinho) {
        return carrinhoMapper.paraResponse(
                carrinho, itensRetirados(carrinho.getId()), prazoBloqueio);
    }

    private long valor(Long consulta) {
        return consulta == null ? 0L : consulta;
    }

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
                .filter(item -> item.getInstrumento().getId().equals(instrumentoId))
                .findFirst()
                .orElse(null);
    }

    private ItemCarrinho itemDoCarrinho(Carrinho carrinho, Long itemId) {
        return carrinho.getItens().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
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
