package com.gakki.store.service;

import com.gakki.store.domain.cliente.Cliente;
import com.gakki.store.domain.recomendacao.ConversaChat;
import com.gakki.store.domain.recomendacao.FeedbackResposta;
import com.gakki.store.domain.recomendacao.MensagemChat;
import com.gakki.store.domain.recomendacao.enums.AvaliacaoResposta;
import com.gakki.store.domain.vendas.Instrumento;
import com.gakki.store.dto.response.ConversaResponse;
import com.gakki.store.dto.response.MensagemChatResponse;
import com.gakki.store.dto.response.PaginaResponse;
import com.gakki.store.dto.response.RespostaChatResponse;
import com.gakki.store.exception.RecursoNaoEncontradoException;
import com.gakki.store.exception.RegraDeNegocioException;
import com.gakki.store.mapper.ChatMapper;
import com.gakki.store.repository.ClienteRepository;
import com.gakki.store.repository.ConversaChatRepository;
import com.gakki.store.repository.FeedbackRespostaRepository;
import com.gakki.store.repository.InstrumentoRepository;
import com.gakki.store.repository.MensagemChatRepository;
import com.gakki.store.service.ChatRegistroService.PerguntaRegistrada;
import com.gakki.store.service.ia.RespostaIa;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

// RNF0044 — chatbot do cliente: conversa, perguntas à IA e avaliação das respostas.

@Service
@RequiredArgsConstructor
public class ChatService {

    static final String AVISO_INDISPONIVEL =
            "O assistente está indisponível no momento. Tente novamente em instantes.";

    private static final int MAXIMO_PRODUTOS_CITADOS = 3;

    private final ClienteRepository clienteRepository;
    private final ConversaChatRepository conversaRepository;
    private final MensagemChatRepository mensagemRepository;
    private final FeedbackRespostaRepository feedbackRepository;
    private final InstrumentoRepository instrumentoRepository;
    private final ChatRegistroService registroService;
    private final AssistenteIaService assistenteIaService;
    private final ChatMapper chatMapper;

    // A conversa em andamento é a mais recente; vazio se o cliente nunca conversou.
    @Transactional(readOnly = true)
    public Optional<ConversaResponse> conversaAtual(String email) {
        Long clienteId = clienteDoEmail(email).getId();
        return conversaRepository.findFirstByClienteIdOrderByIniciadaEmDescIdDesc(clienteId)
                .map(chatMapper::paraResponse);
    }

    @Transactional
    public ConversaResponse novaConversa(String email) {
        ConversaChat conversa = conversaRepository.save(new ConversaChat(clienteDoEmail(email)));
        return chatMapper.paraResponse(conversa);
    }

    // Mais recentes primeiro, paginadas; a tela inverte para exibir.
    @Transactional(readOnly = true)
    public PaginaResponse<MensagemChatResponse> mensagens(String email, Long conversaId, Pageable paginacao) {
        Long clienteId = clienteDoEmail(email).getId();
        conversaRepository.findByIdAndClienteId(conversaId, clienteId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Conversa não encontrada: " + conversaId));

        Page<MensagemChat> pagina = mensagemRepository.findByConversaIdOrderByCriadoEmDescIdDesc(conversaId, paginacao);

        // Avaliações da página numa consulta só, em vez de uma por mensagem.
        Map<Long, AvaliacaoResposta> avaliacoes = feedbackRepository
                .findByMensagemIdIn(pagina.map(MensagemChat::getId).getContent()).stream()
                .collect(Collectors.toMap(f -> f.getMensagem().getId(), FeedbackResposta::getAvaliacao));

        List<Instrumento> catalogo = instrumentoRepository.findAllByAtivoTrueOrderByNomeAsc();
        return PaginaResponse.de(pagina.map(m ->
                chatMapper.paraResponse(m, avaliacoes.get(m.getId()), produtosCitados(m, catalogo))));
    }

    // Sem @Transactional: a IA leva segundos para responder. A pergunta é
    // gravada antes; se a IA falhar, ela fica registrada e o cliente recebe um aviso.
    public RespostaChatResponse enviar(String email, Long conversaId, String texto) {
        Long clienteId = clienteDoEmail(email).getId();
        String pergunta = texto.trim();

        PerguntaRegistrada registrada = registroService.registrarPergunta(clienteId, conversaId, pergunta);
        MensagemChatResponse perguntaResponse = chatMapper.paraResponse(registrada.pergunta(), null, List.of());

        Optional<RespostaIa> resposta = assistenteIaService.responder(clienteId, registrada.anteriores(), pergunta);
        if (resposta.isEmpty()) {
            return new RespostaChatResponse(perguntaResponse, null, AVISO_INDISPONIVEL);
        }

        MensagemChat salva = registroService.registrarResposta(
                conversaId, resposta.get().texto(), resposta.get().versao());
        List<Instrumento> catalogo = instrumentoRepository.findAllByAtivoTrueOrderByNomeAsc();
        return new RespostaChatResponse(
                perguntaResponse, chatMapper.paraResponse(salva, null, produtosCitados(salva, catalogo)), null);
    }

    // RNF0044, critério d — avaliar de novo troca a avaliação anterior.
    @Transactional
    public MensagemChatResponse avaliar(String email, Long mensagemId, AvaliacaoResposta avaliacao) {
        Cliente cliente = clienteDoEmail(email);
        MensagemChat mensagem = mensagemRepository.findByIdAndConversaClienteId(mensagemId, cliente.getId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Mensagem não encontrada: " + mensagemId));
        if (!mensagem.ehDoAssistente()) {
            throw new RegraDeNegocioException("Só as respostas do assistente podem ser avaliadas.");
        }

        FeedbackResposta feedback = feedbackRepository.findByMensagemId(mensagemId)
                .orElseGet(() -> feedbackRepository.save(new FeedbackResposta(mensagem, cliente, avaliacao)));
        feedback.reavaliar(avaliacao);

        List<Instrumento> catalogo = instrumentoRepository.findAllByAtivoTrueOrderByNomeAsc();
        return chatMapper.paraResponse(mensagem, feedback.getAvaliacao(), produtosCitados(mensagem, catalogo));
    }

    // Produtos que a resposta cita, pelo código ou pelo nome completo, na ordem
    // em que aparecem. Não depende de a IA seguir o formato [INST-0000].
    List<Instrumento> produtosCitados(MensagemChat mensagem, List<Instrumento> catalogo) {
        if (!mensagem.ehDoAssistente()) {
            return List.of();
        }
        String texto = mensagem.getTexto().toLowerCase(Locale.ROOT);
        return catalogo.stream()
                .map(instrumento -> Map.entry(instrumento, posicaoNoTexto(texto, instrumento)))
                .filter(citado -> citado.getValue() >= 0)
                .sorted(Comparator.comparingInt(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .limit(MAXIMO_PRODUTOS_CITADOS)
                .toList();
    }

    private static int posicaoNoTexto(String texto, Instrumento instrumento) {
        int porCodigo = texto.indexOf(instrumento.getCodigo().toLowerCase(Locale.ROOT));
        int porNome = texto.indexOf(instrumento.getNome().toLowerCase(Locale.ROOT));
        if (porCodigo < 0) {
            return porNome;
        }
        return porNome < 0 ? porCodigo : Math.min(porCodigo, porNome);
    }

    private Cliente clienteDoEmail(String email) {
        return clienteRepository.findByUsuarioEmail(email)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Nenhum cadastro de cliente associado a este usuário."));
    }
}
