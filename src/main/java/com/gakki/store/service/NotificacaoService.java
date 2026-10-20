package com.gakki.store.service;

import com.gakki.store.domain.cliente.Cliente;
import com.gakki.store.domain.cliente.Notificacao;
import com.gakki.store.dto.response.NotificacaoResponse;
import com.gakki.store.dto.response.PaginaResponse;
import com.gakki.store.exception.RecursoNaoEncontradoException;
import com.gakki.store.mapper.TrocaMapper;
import com.gakki.store.repository.ClienteRepository;
import com.gakki.store.repository.NotificacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/*
 * RN0046 — geração e leitura de notificações.
 *
 * Service próprio, e não um método solto no TrocaService, porque a
 * notificação não pertence ao fluxo de troca: a RN0046 é apenas o
 * primeiro emissor. Quando outra regra precisar notificar, chama daqui.
 */
@Service
@RequiredArgsConstructor
public class NotificacaoService {

    private final NotificacaoRepository notificacaoRepository;
    private final ClienteRepository clienteRepository;
    private final TrocaMapper trocaMapper;

    /*
     * Sem @Transactional próprio de propósito: participa da transação de
     * quem notifica. Se a autorização da troca falhar depois disto, a
     * notificação não pode sobreviver sozinha — o cliente seria avisado
     * de algo que não aconteceu.
     */
    public void notificar(Cliente cliente, String mensagem) {
        Notificacao notificacao = new Notificacao();
        notificacao.setCliente(cliente);
        notificacao.setMensagem(mensagem);
        notificacao.setLida(false);
        notificacaoRepository.save(notificacao);
    }

    @Transactional(readOnly = true)
    public PaginaResponse<NotificacaoResponse> minhasNotificacoes(String email, Pageable paginacao) {
        Long clienteId = clienteDoEmail(email).getId();
        return PaginaResponse.de(
                notificacaoRepository.findByClienteIdOrderByCriadoEmDesc(clienteId, paginacao)
                        .map(trocaMapper::paraResponse));
    }

    private Cliente clienteDoEmail(String email) {
        return clienteRepository.findByUsuarioEmail(email)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Nenhum cadastro de cliente associado a este usuário."));
    }
}
