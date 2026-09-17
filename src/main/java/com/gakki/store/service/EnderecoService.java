package com.gakki.store.service;

import com.gakki.store.domain.Cliente;
import com.gakki.store.domain.Endereco;
import com.gakki.store.dto.request.EnderecoRequest;
import com.gakki.store.dto.response.EnderecoResponse;
import com.gakki.store.exception.ConflitoException;
import com.gakki.store.exception.RecursoNaoEncontradoException;
import com.gakki.store.exception.RegraDeNegocioException;
import com.gakki.store.mapper.EnderecoMapper;
import com.gakki.store.repository.ClienteRepository;
import com.gakki.store.repository.EnderecoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * RF0026 — endereços do cliente, com as garantias da RN0021, RN0022 e
 * RN0023.
 */
@Service
@RequiredArgsConstructor
public class EnderecoService {

    private final EnderecoRepository enderecoRepository;
    private final ClienteRepository clienteRepository;
    private final EnderecoMapper enderecoMapper;

    /**
     * Endereço criado junto com o cadastro do cliente.
     *
     * <p>Nasce marcado como entrega <b>e</b> cobrança, e como principal.
     * É o que satisfaz a RN0021 e a RN0022 com um registro só — as duas
     * exigem ao menos um endereço de cada tipo, e o caso comum é o mesmo
     * endereço servir aos dois. As marcações do payload são ignoradas
     * aqui de propósito: um cadastro que chegasse com {@code entrega:
     * false} criaria um cliente que nasce violando a RN0022.
     *
     * <p>Sem {@code @Transactional} próprio: roda dentro da transação do
     * cadastro, para o endereço e o cliente nascerem ou falharem juntos.
     */
    public Endereco criarNoCadastro(Cliente cliente, EnderecoRequest requisicao) {
        Endereco endereco = enderecoMapper.paraEntidade(requisicao, cliente);
        endereco.setEntrega(true);
        endereco.setCobranca(true);
        endereco.setPrincipal(true);
        return enderecoRepository.save(endereco);
    }

    @Transactional(readOnly = true)
    public List<EnderecoResponse> listar(String email) {
        Long clienteId = clienteDoEmail(email).getId();
        return enderecoRepository.findByClienteIdOrderByPrincipalDescIdAsc(clienteId).stream()
                .map(enderecoMapper::paraResponse)
                .toList();
    }

    /** RF0026 — o cliente pode ter diversos endereços. */
    @Transactional
    public EnderecoResponse adicionar(String email, EnderecoRequest requisicao) {
        Cliente cliente = clienteDoEmail(email);
        validarFinalidade(requisicao);

        Endereco endereco = enderecoMapper.paraEntidade(requisicao, cliente);

        // Se o cliente ainda não tem um principal e este é de entrega,
        // ele assume. Evita a situação de o checkout não ter nenhum
        // endereço sugerido.
        boolean semPrincipal = enderecoRepository
                .findByClienteIdOrderByPrincipalDescIdAsc(cliente.getId()).stream()
                .noneMatch(Endereco::isPrincipal);
        endereco.setPrincipal(semPrincipal && requisicao.entrega());

        return enderecoMapper.paraResponse(enderecoRepository.save(endereco));
    }

    /** RNF0034 — alteração do endereço sem tocar no resto do cadastro. */
    @Transactional
    public EnderecoResponse atualizar(String email, Long enderecoId, EnderecoRequest requisicao) {
        Cliente cliente = clienteDoEmail(email);
        Endereco endereco = buscarDoCliente(enderecoId, cliente.getId());

        validarFinalidade(requisicao);
        validarTrocaDeFinalidade(endereco, requisicao, cliente.getId());

        enderecoMapper.aplicar(requisicao, endereco);
        endereco.setEntrega(requisicao.entrega());
        endereco.setCobranca(requisicao.cobranca());

        return enderecoMapper.paraResponse(endereco);
    }

    /**
     * Exclusão física do endereço — diferente da inativação do cliente.
     *
     * <p>Endereço é dado acessório: sai do banco de verdade. O cadastro
     * do cliente nunca é excluído (RF0023). Essa assimetria é
     * deliberada, e é o que o enunciado chama de "distinção entre
     * inativação e exclusão".
     */
    @Transactional
    public void remover(String email, Long enderecoId) {
        Cliente cliente = clienteDoEmail(email);
        Endereco endereco = buscarDoCliente(enderecoId, cliente.getId());

        // RN0022 — o cliente não pode ficar sem endereço de entrega.
        if (endereco.isEntrega()
                && enderecoRepository.countByClienteIdAndEntregaTrue(cliente.getId()) <= 1) {
            throw new ConflitoException(
                    "Este é o único endereço de entrega do cadastro e não pode ser removido.");
        }

        // RN0021 — nem sem endereço de cobrança.
        if (endereco.isCobranca()
                && enderecoRepository.countByClienteIdAndCobrancaTrue(cliente.getId()) <= 1) {
            throw new ConflitoException(
                    "Este é o único endereço de cobrança do cadastro e não pode ser removido.");
        }

        boolean eraPrincipal = endereco.isPrincipal();
        enderecoRepository.delete(endereco);

        // Removido o principal, o próximo de entrega assume. Sem isso o
        // cliente ficaria com endereços e nenhum sugerido no checkout.
        if (eraPrincipal) {
            enderecoRepository.flush();
            enderecoRepository
                    .findFirstByClienteIdAndEntregaTrueAndIdNotOrderByIdAsc(cliente.getId(), enderecoId)
                    .ifPresent(proximo -> proximo.setPrincipal(true));
        }
    }

    /** RF0026 — um endereço principal por cliente. */
    @Transactional
    public void definirPrincipal(String email, Long enderecoId) {
        Cliente cliente = clienteDoEmail(email);
        Endereco endereco = buscarDoCliente(enderecoId, cliente.getId());

        if (!endereco.isEntrega()) {
            throw new RegraDeNegocioException(
                    "Apenas um endereço de entrega pode ser definido como principal.");
        }

        // A ordem é obrigatória: o índice ux_endereco_principal não
        // admite dois principais no mesmo cliente, então o antigo sai
        // antes de o novo entrar.
        enderecoRepository.desmarcarPrincipal(cliente.getId());

        // Nova busca porque o UPDATE em massa limpa o contexto de
        // persistência — a instância anterior ficou desanexada, e mexer
        // nela não geraria UPDATE nenhum.
        buscarDoCliente(enderecoId, cliente.getId()).setPrincipal(true);
    }

    /** RN0023 — um endereço precisa servir a alguma finalidade. */
    private void validarFinalidade(EnderecoRequest requisicao) {
        if (!requisicao.entrega() && !requisicao.cobranca()) {
            throw new RegraDeNegocioException(
                    "O endereço deve ser de entrega, de cobrança ou de ambos.");
        }
    }

    /**
     * Impede que uma alteração de finalidade viole a RN0021 ou a RN0022.
     *
     * <p>Desmarcar "entrega" do único endereço de entrega tem o mesmo
     * efeito de removê-lo, então recebe o mesmo tratamento.
     */
    private void validarTrocaDeFinalidade(Endereco endereco, EnderecoRequest requisicao, Long clienteId) {
        if (endereco.isEntrega() && !requisicao.entrega()) {
            if (enderecoRepository.countByClienteIdAndEntregaTrue(clienteId) <= 1) {
                throw new ConflitoException(
                        "Este é o único endereço de entrega do cadastro e precisa continuar sendo.");
            }
            if (endereco.isPrincipal()) {
                throw new ConflitoException(
                        "O endereço principal precisa ser de entrega. Defina outro como principal antes.");
            }
        }

        if (endereco.isCobranca() && !requisicao.cobranca()
                && enderecoRepository.countByClienteIdAndCobrancaTrue(clienteId) <= 1) {
            throw new ConflitoException(
                    "Este é o único endereço de cobrança do cadastro e precisa continuar sendo.");
        }
    }

    private Endereco buscarDoCliente(Long enderecoId, Long clienteId) {
        return enderecoRepository.findByIdAndClienteId(enderecoId, clienteId)
                .orElseThrow(() -> RecursoNaoEncontradoException.endereco(enderecoId));
    }

    private Cliente clienteDoEmail(String email) {
        return clienteRepository.findByUsuarioEmail(email)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Nenhum cadastro de cliente associado a este usuário."));
    }
}
