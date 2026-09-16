package com.gakki.store.service;

import com.gakki.store.domain.Bandeira;
import com.gakki.store.domain.Cartao;
import com.gakki.store.domain.Cliente;
import com.gakki.store.dto.request.CartaoRequest;
import com.gakki.store.dto.response.BandeiraResponse;
import com.gakki.store.exception.RegraDeNegocioException;
import com.gakki.store.mapper.CartaoMapper;
import com.gakki.store.repository.BandeiraRepository;
import com.gakki.store.repository.CartaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartaoService {

    private final CartaoRepository cartaoRepository;
    private final BandeiraRepository bandeiraRepository;
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
}
