package com.gakki.store.service;

import com.gakki.store.domain.cliente.Bandeira;
import com.gakki.store.domain.cliente.Cartao;
import com.gakki.store.domain.cliente.Cliente;
import com.gakki.store.dto.request.CartaoRequest;
import com.gakki.store.dto.response.BandeiraResponse;
import com.gakki.store.dto.response.CartaoResponse;
import com.gakki.store.exception.RecursoNaoEncontradoException;
import com.gakki.store.exception.RegraDeNegocioException;
import com.gakki.store.mapper.CartaoMapper;
import com.gakki.store.repository.BandeiraRepository;
import com.gakki.store.repository.CartaoRepository;
import com.gakki.store.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.List;

// RF0027 - cartões do cliente, com as garantias da RN0024 e RN0025.

@Service
@RequiredArgsConstructor
public class CartaoService {

    private final CartaoRepository cartaoRepository;
    private final BandeiraRepository bandeiraRepository;
    private final ClienteRepository clienteRepository;
    private final CartaoMapper cartaoMapper;

    // Cartão informado no cadastro — opcional (RF0027).
    public Cartao criarNoCadastro(Cliente cliente, CartaoRequest requisicao) {
        Bandeira bandeira = buscarBandeiraValida(requisicao.bandeiraId());
        validarValidade(requisicao);

        Cartao cartao = cartaoMapper.paraEntidade(requisicao, cliente, bandeira);
        cartao.setPreferencial(true);
        return cartaoRepository.save(cartao);
    }

    @Transactional(readOnly = true)
    public List<CartaoResponse> listar(String email) {
        Long clienteId = clienteDoEmail(email).getId();
        return cartaoRepository.findByClienteIdOrderByPreferencialDescIdAsc(clienteId).stream()
                .map(cartaoMapper::paraResponse)
                .toList();
    }

    // RF0027 — o cliente pode ter diversos cartões.
    @Transactional
    public CartaoResponse adicionar(String email, CartaoRequest requisicao) {
        Cliente cliente = clienteDoEmail(email);
        Bandeira bandeira = buscarBandeiraValida(requisicao.bandeiraId());
        validarValidade(requisicao);

        Cartao cartao = cartaoMapper.paraEntidade(requisicao, cliente, bandeira);

        boolean primeiro = cartaoRepository
                .findByClienteIdOrderByPreferencialDescIdAsc(cliente.getId()).isEmpty();
        cartao.setPreferencial(primeiro);

        return cartaoMapper.paraResponse(cartaoRepository.save(cartao));
    }

    @Transactional
    public CartaoResponse atualizar(String email, Long cartaoId, CartaoRequest requisicao) {
        Cliente cliente = clienteDoEmail(email);
        Cartao cartao = buscarDoCliente(cartaoId, cliente.getId());

        Bandeira bandeira = buscarBandeiraValida(requisicao.bandeiraId());
        validarValidade(requisicao);

        cartaoMapper.aplicar(requisicao, cartao, bandeira);
        return cartaoMapper.paraResponse(cartao);
    }

    // Exclusão física do cartão.
    @Transactional
    public void remover(String email, Long cartaoId) {
        Cliente cliente = clienteDoEmail(email);
        Cartao cartao = buscarDoCliente(cartaoId, cliente.getId());

        boolean eraPreferencial = cartao.isPreferencial();
        cartaoRepository.delete(cartao);

        if (eraPreferencial) {
            cartaoRepository.flush();
            cartaoRepository
                    .findFirstByClienteIdAndIdNotOrderByIdAsc(cliente.getId(), cartaoId)
                    .ifPresent(proximo -> proximo.setPreferencial(true));
        }
    }

    // RF0027 — deve haver um cartão configurado como preferencial.
    @Transactional
    public void definirPreferencial(String email, Long cartaoId) {
        Cliente cliente = clienteDoEmail(email);
        buscarDoCliente(cartaoId, cliente.getId());
        cartaoRepository.desmarcarPreferencial(cliente.getId());
        buscarDoCliente(cartaoId, cliente.getId()).setPreferencial(true);
    }

    // RN0025 - a bandeira precisa estar registrada e ativa no sistema.
    public Bandeira buscarBandeiraValida(Long bandeiraId) {
        return bandeiraRepository.findByIdAndAtivoTrue(bandeiraId)
                .orElseThrow(() -> new RegraDeNegocioException(
                        "Bandeira não registrada ou inativa no sistema."));
    }

    @Transactional(readOnly = true)
    public List<BandeiraResponse> listarBandeiras() {
        return bandeiraRepository.findByAtivoTrueOrderByNomeAsc().stream()
                .map(cartaoMapper::paraResponse)
                .toList();
    }

    // Cartão vencido não entra.
    private void validarValidade(CartaoRequest requisicao) {
        YearMonth validade = YearMonth.of(requisicao.validadeAno(), requisicao.validadeMes());
        if (validade.isBefore(YearMonth.now())) {
            throw new RegraDeNegocioException("O cartão informado está vencido.");
        }
    }

    private Cartao buscarDoCliente(Long cartaoId, Long clienteId) {
        return cartaoRepository.findByIdAndClienteId(cartaoId, clienteId)
                .orElseThrow(() -> RecursoNaoEncontradoException.cartao(cartaoId));
    }

    private Cliente clienteDoEmail(String email) {
        return clienteRepository.findByUsuarioEmail(email)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Nenhum cadastro de cliente associado a este usuário."));
    }
}
