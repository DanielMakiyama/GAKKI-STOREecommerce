package com.gakki.store.mapper;

import com.gakki.store.domain.recomendacao.ConversaChat;
import com.gakki.store.domain.recomendacao.MensagemChat;
import com.gakki.store.domain.recomendacao.enums.AvaliacaoResposta;
import com.gakki.store.domain.vendas.Instrumento;
import com.gakki.store.dto.response.ConversaResponse;
import com.gakki.store.dto.response.MensagemChatResponse;
import com.gakki.store.dto.response.ProdutoCitadoResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ChatMapper {

    public ConversaResponse paraResponse(ConversaChat conversa) {
        return new ConversaResponse(conversa.getId(), conversa.getIniciadaEm());
    }

    public MensagemChatResponse paraResponse(MensagemChat mensagem, AvaliacaoResposta avaliacao,
                                             List<Instrumento> produtosCitados) {
        return new MensagemChatResponse(
                mensagem.getId(),
                mensagem.getAutor(),
                mensagem.getTexto(),
                mensagem.getCriadoEm(),
                avaliacao,
                produtosCitados.stream().map(this::paraProduto).toList());
    }

    private ProdutoCitadoResponse paraProduto(Instrumento instrumento) {
        return new ProdutoCitadoResponse(
                instrumento.getId(),
                instrumento.getCodigo(),
                instrumento.getNome(),
                instrumento.getValorVenda());
    }
}
