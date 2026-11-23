package com.gakki.store.service;

import com.gakki.store.config.IaProperties;
import com.gakki.store.domain.recomendacao.ModeloIa;
import com.gakki.store.dto.request.SimulacaoIaRequest;
import com.gakki.store.dto.response.IaStatusResponse;
import com.gakki.store.dto.response.SimulacaoIaResponse;
import com.gakki.store.exception.IaIndisponivelException;
import com.gakki.store.exception.RecursoNaoEncontradoException;
import com.gakki.store.repository.ClienteRepository;
import com.gakki.store.repository.ModeloIaRepository;
import com.gakki.store.service.ia.MensagemIa;
import com.gakki.store.service.ia.ProvedorIa;
import com.gakki.store.service.ia.RespostaIa;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// RNF0044 — ponto único de acesso à IA. Escolhe o modelo (versão ajustada
// ativa ou o base), monta a conversa e degrada em silêncio se a IA falhar.
// Sem @Transactional: a resposta leva segundos e não deve prender conexão do banco.

@Service
@RequiredArgsConstructor
public class AssistenteIaService {

    private static final Logger log = LoggerFactory.getLogger(AssistenteIaService.class);

    private final ProvedorIa provedorIa;
    private final ContextoRecomendacaoService contextoService;
    private final ModeloIaRepository modeloIaRepository;
    private final ClienteRepository clienteRepository;
    private final IaProperties propriedades;

    // Usado pelo chat. Vazio quando a IA não respondeu; quem chama decide o que mostrar.
    public Optional<RespostaIa> responder(Long clienteId, List<MensagemIa> anteriores, String pergunta) {
        return conversar(contextoService.instrucoesDoSistema(clienteId), anteriores, pergunta);
    }

    // Administrador pergunta em nome de um cliente e vê o contexto enviado.
    public SimulacaoIaResponse simular(SimulacaoIaRequest requisicao) {
        Long clienteId = clienteRepository.findByUsuarioEmail(requisicao.email())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Nenhum cliente com o e-mail " + requisicao.email() + "."))
                .getId();

        String instrucoes = contextoService.instrucoesDoSistema(clienteId);
        long inicio = System.currentTimeMillis();

        RespostaIa resposta = conversar(instrucoes, List.of(), requisicao.pergunta())
                .orElseThrow(() -> new IaIndisponivelException(
                        "O assistente não respondeu. Verifique se o Ollama está em execução."));

        return new SimulacaoIaResponse(
                resposta.texto(),
                resposta.modelo(),
                resposta.versao() == null ? null : resposta.versao().getVersao(),
                System.currentTimeMillis() - inicio,
                instrucoes);
    }

    // Cenário 1 da apresentação: mostra que a integração está no ar e com qual modelo.
    public IaStatusResponse status() {
        Optional<ModeloIa> ativo = modeloIaRepository.findByAtivoTrue();
        String emUso = ativo.map(ModeloIa::getIdentificador).orElse(propriedades.modeloBase());

        List<String> instalados;
        boolean online;
        try {
            instalados = provedorIa.modelosInstalados();
            online = true;
        } catch (IaIndisponivelException e) {
            instalados = List.of();
            online = false;
        }

        return new IaStatusResponse(
                provedorIa.nome(),
                propriedades.url(),
                online,
                emUso,
                ativo.map(ModeloIa::getVersao).orElse(null),
                instalados.contains(emUso),
                instalados);
    }

    // Carrega o modelo e faz a IA ler regras e catálogo, gerando só 1 token.
    // A primeira pergunta de um cliente passa a processar apenas o trecho dele.
    public void aquecer() {
        String modelo = modeloIaRepository.findByAtivoTrue()
                .map(ModeloIa::getIdentificador).orElse(propriedades.modeloBase());
        long inicio = System.currentTimeMillis();
        try {
            provedorIa.conversar(modelo, List.of(
                    MensagemIa.sistema(contextoService.instrucoesGerais()),
                    MensagemIa.usuario("Olá")), 1);
            log.info("IA aquecida (modelo {}) em {} s", modelo, (System.currentTimeMillis() - inicio) / 1000);
        } catch (IaIndisponivelException e) {
            log.warn("Não foi possível aquecer a IA (modelo {}): {}", modelo, e.getMessage());
        }
    }

    private Optional<RespostaIa> conversar(String instrucoes, List<MensagemIa> anteriores, String pergunta) {
        ModeloIa ativo = modeloIaRepository.findByAtivoTrue().orElse(null);
        String modelo = ativo != null ? ativo.getIdentificador() : propriedades.modeloBase();

        List<MensagemIa> mensagens = new ArrayList<>();
        mensagens.add(MensagemIa.sistema(instrucoes));
        mensagens.addAll(anteriores);
        mensagens.add(MensagemIa.usuario(pergunta));

        try {
            String texto = provedorIa.conversar(modelo, mensagens, propriedades.maxTokens());
            return Optional.of(new RespostaIa(texto, modelo, ativo));
        } catch (IaIndisponivelException e) {
            log.warn("IA indisponível (modelo {}): {} | causa: {}", modelo, e.getMessage(),
                    e.getCause() == null ? "-" : e.getCause().toString());
            return Optional.empty();
        }
    }
}
