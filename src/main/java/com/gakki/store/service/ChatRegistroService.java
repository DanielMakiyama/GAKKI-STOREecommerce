package com.gakki.store.service;

import com.gakki.store.domain.recomendacao.ConversaChat;
import com.gakki.store.domain.recomendacao.MensagemChat;
import com.gakki.store.domain.recomendacao.ModeloIa;
import com.gakki.store.exception.RecursoNaoEncontradoException;
import com.gakki.store.repository.ConversaChatRepository;
import com.gakki.store.repository.MensagemChatRepository;
import com.gakki.store.repository.ModeloIaRepository;
import com.gakki.store.service.ia.MensagemIa;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// RNF0044 — gravações do chat, cada uma na sua transação. Fica separado do
// ChatService porque a chamada à IA, entre as duas, não pode estar dentro de
// uma transação (levaria até um minuto segurando a conexão do banco).

@Service
@RequiredArgsConstructor
public class ChatRegistroService {

    private final ConversaChatRepository conversaRepository;
    private final MensagemChatRepository mensagemRepository;
    private final ModeloIaRepository modeloIaRepository;

    // Grava a pergunta e devolve as mensagens anteriores, para a IA manter o assunto.
    @Transactional
    public PerguntaRegistrada registrarPergunta(Long clienteId, Long conversaId, String texto) {
        ConversaChat conversa = conversaRepository.findByIdAndClienteId(conversaId, clienteId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Conversa não encontrada: " + conversaId));

        List<MensagemIa> anteriores = mensagemRepository.findTop12ByConversaIdOrderByCriadoEmDescIdDesc(conversaId)
                .reversed().stream()
                .map(m -> m.ehDoAssistente() ? MensagemIa.assistente(m.getTexto()) : MensagemIa.usuario(m.getTexto()))
                .toList();

        MensagemChat pergunta = mensagemRepository.save(MensagemChat.doCliente(conversa, texto));
        return new PerguntaRegistrada(pergunta, anteriores);
    }

    // versao nula = respondida pelo modelo base, antes de qualquer ajuste fino.
    @Transactional
    public MensagemChat registrarResposta(Long conversaId, String texto, ModeloIa versao) {
        ConversaChat conversa = conversaRepository.getReferenceById(conversaId);
        ModeloIa modelo = versao == null ? null : modeloIaRepository.getReferenceById(versao.getId());
        return mensagemRepository.save(MensagemChat.doAssistente(conversa, texto, modelo));
    }

    public record PerguntaRegistrada(MensagemChat pergunta, List<MensagemIa> anteriores) {
    }
}
