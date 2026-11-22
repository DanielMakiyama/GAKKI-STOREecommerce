package com.gakki.store.service;

import com.gakki.store.domain.cliente.Cliente;
import com.gakki.store.domain.recomendacao.PreferenciaCliente;
import com.gakki.store.domain.vendas.Categoria;
import com.gakki.store.dto.request.PreferenciaClienteRequest;
import com.gakki.store.dto.response.PreferenciaClienteResponse;
import com.gakki.store.exception.RecursoNaoEncontradoException;
import com.gakki.store.exception.RegraDeNegocioException;
import com.gakki.store.mapper.PreferenciaClienteMapper;
import com.gakki.store.repository.CategoriaRepository;
import com.gakki.store.repository.ClienteRepository;
import com.gakki.store.repository.PreferenciaClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

// RNF0044, critério e — preferências obtidas por formulário no perfil.

@Service
@RequiredArgsConstructor
public class PreferenciaClienteService {

    private final PreferenciaClienteRepository preferenciaRepository;
    private final ClienteRepository clienteRepository;
    private final CategoriaRepository categoriaRepository;
    private final PreferenciaClienteMapper preferenciaMapper;

    // Vazio quando o cliente ainda não preencheu o formulário.
    @Transactional(readOnly = true)
    public Optional<PreferenciaClienteResponse> buscar(String email) {
        Long clienteId = clienteDoEmail(email).getId();
        return preferenciaRepository.findByClienteId(clienteId)
                .map(preferenciaMapper::paraResponse);
    }

    // Cria na primeira vez e substitui nas seguintes: uma preferência por cliente.
    @Transactional
    public PreferenciaClienteResponse salvar(String email, PreferenciaClienteRequest requisicao) {
        Cliente cliente = clienteDoEmail(email);
        Set<Categoria> categorias = buscarCategorias(requisicao.categoriaIds());

        PreferenciaCliente preferencia = preferenciaRepository.findByClienteId(cliente.getId())
                .orElseGet(() -> preferenciaMapper.novaPara(cliente));
        preferenciaMapper.aplicar(requisicao, preferencia, categorias);

        // Flush para a resposta já levar o atualizadoEm gravado.
        return preferenciaMapper.paraResponse(preferenciaRepository.saveAndFlush(preferencia));
    }

    private Set<Categoria> buscarCategorias(Set<Long> ids) {
        List<Categoria> encontradas = categoriaRepository.findAllById(ids);
        if (encontradas.size() != ids.size()) {
            Set<Long> existentes = encontradas.stream().map(Categoria::getId).collect(Collectors.toSet());
            String faltando = ids.stream()
                    .filter(id -> !existentes.contains(id))
                    .sorted()
                    .map(String::valueOf)
                    .collect(Collectors.joining(", "));
            throw new RegraDeNegocioException("Categoria não cadastrada: " + faltando + ".");
        }
        return new HashSet<>(encontradas);
    }

    private Cliente clienteDoEmail(String email) {
        return clienteRepository.findByUsuarioEmail(email)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Nenhum cadastro de cliente associado a este usuário."));
    }
}
