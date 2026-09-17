package com.gakki.store.service;

import com.gakki.store.domain.Bandeira;
import com.gakki.store.domain.Cartao;
import com.gakki.store.domain.Cliente;
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

/**
 * RF0027 — cartões do cliente, com as garantias da RN0024 e RN0025.
 *
 * <p>Diferença importante em relação ao endereço: <b>não existe regra de
 * mínimo</b>. A RN0021 e a RN0022 exigem endereço; nenhuma regra exige
 * cartão. Por isso remover o último cartão é permitido, enquanto remover
 * o último endereço de entrega devolve 409. As duas telas parecem
 * simétricas, as regras não são.
 */
@Service
@RequiredArgsConstructor
public class CartaoService {

    private final CartaoRepository cartaoRepository;
    private final BandeiraRepository bandeiraRepository;
    private final ClienteRepository clienteRepository;
    private final CartaoMapper cartaoMapper;

    /**
     * Cartão informado no cadastro — opcional (RF0027).
     *
     * <p>Nasce preferencial por ser o único do cliente. Sem
     * {@code @Transactional} próprio: participa da transação do cadastro.
     */
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

    /** RF0027 — o cliente pode ter diversos cartões. */
    @Transactional
    public CartaoResponse adicionar(String email, CartaoRequest requisicao) {
        Cliente cliente = clienteDoEmail(email);
        Bandeira bandeira = buscarBandeiraValida(requisicao.bandeiraId());
        validarValidade(requisicao);

        Cartao cartao = cartaoMapper.paraEntidade(requisicao, cliente, bandeira);

        // Primeiro cartão do cliente assume como preferencial — senão o
        // checkout não teria nenhum sugerido.
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

    /**
     * Exclusão física do cartão.
     *
     * <p>Sem verificação de mínimo: nenhuma regra obriga o cliente a ter
     * cartão. Se o removido era o preferencial, o próximo assume.
     */
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

    /** RF0027 — deve haver um cartão configurado como preferencial. */
    @Transactional
    public void definirPreferencial(String email, Long cartaoId) {
        Cliente cliente = clienteDoEmail(email);
        buscarDoCliente(cartaoId, cliente.getId());

        // Mesmo cuidado do endereço principal: o índice
        // ux_cartao_preferencial não admite dois, então o antigo sai
        // antes de o novo entrar. E a nova busca é necessária porque o
        // UPDATE em massa desanexa as instâncias do contexto.
        cartaoRepository.desmarcarPreferencial(cliente.getId());
        buscarDoCliente(cartaoId, cliente.getId()).setPreferencial(true);
    }

    /** RN0025 — a bandeira precisa estar registrada e ativa no sistema. */
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

    /**
     * Cartão vencido não entra.
     *
     * <p>A validação declarativa garante mês entre 1 e 12 e ano dentro
     * de uma faixa, mas "não estar vencido" depende da data de hoje —
     * regra de negócio, e não de formato.
     */
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
