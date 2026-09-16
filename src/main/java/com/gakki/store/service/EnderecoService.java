package com.gakki.store.service;

import com.gakki.store.domain.Cliente;
import com.gakki.store.domain.Endereco;
import com.gakki.store.dto.request.EnderecoRequest;
import com.gakki.store.mapper.EnderecoMapper;
import com.gakki.store.repository.EnderecoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EnderecoService {

    private final EnderecoRepository enderecoRepository;
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
    public long contarEnderecosDeEntrega(Long clienteId) {
        return enderecoRepository.countByClienteIdAndEntregaTrue(clienteId);
    }
}
