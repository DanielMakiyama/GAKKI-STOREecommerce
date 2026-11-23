package com.gakki.store.service;

import com.gakki.store.config.IaProperties;
import com.gakki.store.domain.recomendacao.ModeloIa;
import com.gakki.store.domain.recomendacao.enums.StatusModeloIa;
import com.gakki.store.dto.response.ModeloIaResponse;
import com.gakki.store.dto.response.PaginaResponse;
import com.gakki.store.dto.response.PreviaTreinamentoResponse;
import com.gakki.store.exception.ConflitoException;
import com.gakki.store.exception.RecursoNaoEncontradoException;
import com.gakki.store.exception.RegraDeNegocioException;
import com.gakki.store.mapper.ModeloIaMapper;
import com.gakki.store.repository.FeedbackRespostaRepository;
import com.gakki.store.repository.HistoricoComprasRepository;
import com.gakki.store.repository.ModeloIaRepository;
import com.gakki.store.service.DadosTreinamentoService.Conjunto;
import com.gakki.store.service.DadosTreinamentoService.Origem;
import com.gakki.store.service.ia.ProvedorIa;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

// RNF0044, critério c — ciclo de vida de cada versão do modelo:
// PREPARANDO (dados gerados) → TREINANDO (no Colab) → PRONTO (importado no
// Ollama) → ativa no chat. O treino em si roda fora do backend, no Colab.

@Service
@RequiredArgsConstructor
public class TreinamentoService {

    private static final OffsetDateTime SEM_CORTE = OffsetDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);

    private final ModeloIaRepository modeloIaRepository;
    private final HistoricoComprasRepository historicoRepository;
    private final FeedbackRespostaRepository feedbackRepository;
    private final DadosTreinamentoService dadosService;
    private final ProvedorIa provedorIa;
    private final IaProperties propriedades;
    private final ModeloIaMapper modeloIaMapper;

    // Cenário 8: quanto entraria num treino agora e quanto é novo desde o último.
    @Transactional(readOnly = true)
    public PreviaTreinamentoResponse previa() {
        Conjunto conjunto = dadosService.gerar(OffsetDateTime.now());
        Optional<ModeloIa> ultima = modeloIaRepository.findTopByOrderByVersaoDesc();
        OffsetDateTime corte = ultima.map(ModeloIa::getDadosAte).orElse(SEM_CORTE);

        return new PreviaTreinamentoResponse(
                conjunto.quantos(Origem.VENDA),
                conjunto.quantos(Origem.FEEDBACK),
                conjunto.quantos(Origem.CATALOGO),
                conjunto.exemplos().size(),
                ultima.map(ModeloIa::getVersao).orElse(null),
                ultima.map(ModeloIa::getDadosAte).orElse(null),
                historicoRepository.contarPedidosDesde(ContextoRecomendacaoService.STATUS_COMPRADO, corte),
                feedbackRepository.contarDesde(corte));
    }

    @Transactional(readOnly = true)
    public PaginaResponse<ModeloIaResponse> listar(Pageable paginacao) {
        return PaginaResponse.de(modeloIaRepository.findAllByOrderByVersaoDesc(paginacao).map(modeloIaMapper::paraResponse));
    }

    // Congela o corte (dados_ate) e registra a nova versão. O arquivo de treino
    // é sempre gerado com esse corte, então baixar de novo dá o mesmo conteúdo.
    @Transactional
    public ModeloIaResponse criar() {
        List<ModeloIa> emAndamento = modeloIaRepository.findByStatus(StatusModeloIa.PREPARANDO);
        if (!emAndamento.isEmpty() || !modeloIaRepository.findByStatus(StatusModeloIa.TREINANDO).isEmpty()) {
            throw new ConflitoException("Já existe um treinamento em andamento. Conclua ou marque como falho antes de criar outro.");
        }

        OffsetDateTime corte = OffsetDateTime.now();
        Conjunto conjunto = dadosService.gerar(corte);
        if (conjunto.exemplos().isEmpty()) {
            throw new RegraDeNegocioException("Não há vendas nem avaliações úteis para treinar.");
        }

        ModeloIa modelo = new ModeloIa();
        modelo.setVersao(modeloIaRepository.findTopByOrderByVersaoDesc().map(m -> m.getVersao() + 1).orElse(1));
        modelo.setProvedor(provedorIa.nome());
        modelo.setModeloBase(propriedades.modeloBase());
        modelo.setStatus(StatusModeloIa.PREPARANDO);
        modelo.setAtivo(false);
        modelo.setExemplosTotal(conjunto.exemplos().size());
        modelo.setExemplosVendas(conjunto.quantos(Origem.VENDA));
        modelo.setExemplosFeedback(conjunto.quantos(Origem.FEEDBACK));
        modelo.setDadosAte(corte);
        return modeloIaMapper.paraResponse(modeloIaRepository.save(modelo));
    }

    @Transactional(readOnly = true)
    public String arquivoDeTreino(Integer versao) {
        ModeloIa modelo = buscar(versao);
        return dadosService.paraJsonl(dadosService.gerar(modelo.getDadosAte()));
    }

    @Transactional
    public ModeloIaResponse iniciar(Integer versao, String jobId) {
        ModeloIa modelo = buscar(versao);
        exigirStatus(modelo, StatusModeloIa.PREPARANDO);
        modelo.iniciarTreinamento(jobId == null || jobId.isBlank() ? null : jobId.trim());
        return modeloIaMapper.paraResponse(modelo);
    }

    // Só conclui se o modelo já existir no Ollama, para o chat não apontar para o vazio.
    @Transactional
    public ModeloIaResponse concluir(Integer versao, String identificador) {
        ModeloIa modelo = buscar(versao);
        exigirStatus(modelo, StatusModeloIa.PREPARANDO, StatusModeloIa.TREINANDO);

        String nome = identificador.trim().contains(":") ? identificador.trim() : identificador.trim() + ":latest";
        if (!provedorIa.modelosInstalados().contains(nome)) {
            throw new RegraDeNegocioException("O modelo " + nome + " não foi encontrado no " + provedorIa.nome()
                    + ". Rode o ollama create antes de concluir.");
        }
        modelo.concluir(nome);
        return modeloIaMapper.paraResponse(modelo);
    }

    @Transactional
    public ModeloIaResponse falhar(Integer versao, String motivo) {
        ModeloIa modelo = buscar(versao);
        exigirStatus(modelo, StatusModeloIa.PREPARANDO, StatusModeloIa.TREINANDO);
        modelo.falhar(motivo.trim());
        return modeloIaMapper.paraResponse(modelo);
    }

    // Uma versão ativa por vez (índice ux_modelo_ia_ativo). A anterior é
    // desativada e gravada antes, senão o banco veria duas ativas no flush.
    @Transactional
    public ModeloIaResponse ativar(Integer versao) {
        ModeloIa modelo = buscar(versao);
        exigirStatus(modelo, StatusModeloIa.PRONTO);

        modeloIaRepository.findByAtivoTrue()
                .filter(atual -> !atual.getId().equals(modelo.getId()))
                .ifPresent(atual -> {
                    atual.setAtivo(false);
                    modeloIaRepository.saveAndFlush(atual);
                });
        modelo.setAtivo(true);
        return modeloIaMapper.paraResponse(modelo);
    }

    // Volta o chat para o modelo base, útil para comparar antes e depois do treino.
    @Transactional
    public ModeloIaResponse desativar(Integer versao) {
        ModeloIa modelo = buscar(versao);
        modelo.setAtivo(false);
        return modeloIaMapper.paraResponse(modelo);
    }

    private ModeloIa buscar(Integer versao) {
        return modeloIaRepository.findByVersao(versao)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Versão do modelo não encontrada: " + versao));
    }

    private static void exigirStatus(ModeloIa modelo, StatusModeloIa... permitidos) {
        for (StatusModeloIa status : permitidos) {
            if (modelo.getStatus() == status) {
                return;
            }
        }
        throw new RegraDeNegocioException("A versão " + modelo.getVersao() + " está " + modelo.getStatus()
                + " e não permite esta operação.");
    }
}
