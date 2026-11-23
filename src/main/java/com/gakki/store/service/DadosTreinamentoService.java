package com.gakki.store.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gakki.store.domain.recomendacao.FeedbackResposta;
import com.gakki.store.domain.recomendacao.MensagemChat;
import com.gakki.store.domain.recomendacao.PreferenciaCliente;
import com.gakki.store.domain.recomendacao.enums.AvaliacaoResposta;
import com.gakki.store.domain.vendas.Categoria;
import com.gakki.store.domain.vendas.Instrumento;
import com.gakki.store.repository.FeedbackRespostaRepository;
import com.gakki.store.repository.HistoricoComprasRepository;
import com.gakki.store.repository.HistoricoComprasRepository.ItemComprado;
import com.gakki.store.repository.InstrumentoRepository;
import com.gakki.store.repository.MensagemChatRepository;
import com.gakki.store.repository.PreferenciaClienteRepository;
import com.gakki.store.service.ia.MensagemIa;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

// RNF0044, critério c — exemplos do ajuste fino, tirados das vendas e do
// feedback útil, no mesmo formato de contexto que o chat envia à IA.
// Respostas "não úteis" ficam de fora: o modelo não deve imitá-las.

@Service
@RequiredArgsConstructor
public class DadosTreinamentoService {

    private static final int MENSAGENS_ANTERIORES = 6;

    private static final Locale PT_BR = Locale.of("pt", "BR");

    private static final List<String> PERGUNTAS = List.of(
            "Me recomende um produto.",
            "O que você sugere para mim agora?",
            "Estou pensando na próxima compra. O que combina comigo?",
            "Tem alguma sugestão de %s para mim?");

    private final HistoricoComprasRepository historicoRepository;
    private final FeedbackRespostaRepository feedbackRepository;
    private final MensagemChatRepository mensagemRepository;
    private final PreferenciaClienteRepository preferenciaRepository;
    private final InstrumentoRepository instrumentoRepository;
    private final ContextoRecomendacaoService contextoService;
    private final ObjectMapper objectMapper;

    public enum Origem { VENDA, FEEDBACK, CATALOGO }

    public record Exemplo(Origem origem, List<MensagemIa> mensagens) {
    }

    public record Conjunto(List<Exemplo> exemplos) {

        public int quantos(Origem origem) {
            return (int) exemplos.stream().filter(e -> e.origem() == origem).count();
        }
    }

    // Tudo o que existia até o corte. Cada versão treina do zero com todos os
    // dados até o seu corte; os dados novos entram na versão seguinte.
    @Transactional(readOnly = true)
    public Conjunto gerar(OffsetDateTime ate) {
        List<Instrumento> catalogo = instrumentoRepository.findAllByAtivoTrueOrderByNomeAsc();
        Map<String, Instrumento> porCodigo = catalogo.stream()
                .collect(Collectors.toMap(Instrumento::getCodigo, Function.identity()));

        Map<Long, List<ItemComprado>> comprasPorCliente = historicoRepository
                .itensDeTodosOsClientesAte(ContextoRecomendacaoService.STATUS_COMPRADO,
                        ContextoRecomendacaoService.FUSO_DA_LOJA, ate).stream()
                .collect(Collectors.groupingBy(ItemComprado::getClienteId, LinkedHashMap::new, Collectors.toList()));

        Map<Long, Optional<PreferenciaCliente>> preferencias = new HashMap<>();
        Function<Long, Optional<PreferenciaCliente>> preferenciaDe =
                id -> preferencias.computeIfAbsent(id, preferenciaRepository::findByClienteId);

        List<Exemplo> exemplos = new ArrayList<>();
        exemplos.addAll(deVendas(comprasPorCliente, preferenciaDe, catalogo, porCodigo));
        exemplos.addAll(deFeedback(ate, comprasPorCliente, preferenciaDe, catalogo));
        exemplos.addAll(doCatalogo(catalogo));
        return new Conjunto(exemplos);
    }

    // Uma linha JSON por exemplo, no formato de chat que o notebook do Colab lê.
    public String paraJsonl(Conjunto conjunto) {
        StringBuilder jsonl = new StringBuilder();
        for (Exemplo exemplo : conjunto.exemplos()) {
            List<Map<String, String>> mensagens = exemplo.mensagens().stream()
                    .map(m -> Map.of("role", papel(m.papel()), "content", m.texto()))
                    .toList();
            try {
                jsonl.append(objectMapper.writeValueAsString(Map.of("messages", mensagens))).append('\n');
            } catch (JsonProcessingException e) {
                throw new IllegalStateException("Falha ao gerar o arquivo de treino.", e);
            }
        }
        return jsonl.toString();
    }

    // Para cada pedido, o produto principal que o cliente ainda não tinha vira
    // a resposta certa, com o histórico de ANTES daquele pedido no contexto.
    private List<Exemplo> deVendas(Map<Long, List<ItemComprado>> comprasPorCliente,
                                   Function<Long, Optional<PreferenciaCliente>> preferenciaDe,
                                   List<Instrumento> catalogo, Map<String, Instrumento> porCodigo) {
        List<Exemplo> exemplos = new ArrayList<>();
        comprasPorCliente.forEach((clienteId, itens) -> {
            Map<String, List<ItemComprado>> porPedido = itens.stream()
                    .collect(Collectors.groupingBy(ItemComprado::getPedido, LinkedHashMap::new, Collectors.toList()));

            List<ItemComprado> antes = new ArrayList<>();
            Set<String> jaTinha = new HashSet<>();
            for (List<ItemComprado> pedido : porPedido.values()) {
                Optional<Instrumento> principal = pedido.stream()
                        .filter(item -> !jaTinha.contains(item.getCodigo()))
                        .max(Comparator.comparing(ItemComprado::getValorUnitario))
                        .map(item -> porCodigo.get(item.getCodigo()));

                // Só entra se o produto passa nos filtros de hoje (estoque, orçamento,
                // categorias): o exemplo não pode ensinar a contrariar a lista.
                Optional<PreferenciaCliente> preferencia = preferenciaDe.apply(clienteId);
                if (principal.isPresent()
                        && contextoService.candidatos(catalogo, preferencia, antes).contains(principal.get())) {
                    Instrumento produto = principal.get();
                    String sistema = contextoService.instrucoes(preferencia, List.copyOf(antes), catalogo, produto);
                    exemplos.add(new Exemplo(Origem.VENDA, List.of(
                            MensagemIa.sistema(sistema),
                            MensagemIa.usuario(pergunta(exemplos.size(), produto)),
                            MensagemIa.assistente(recomendacao(produto, antes)))));
                }
                pedido.forEach(item -> jaTinha.add(item.getCodigo()));
                antes.addAll(pedido);
            }
        });
        return exemplos;
    }

    // A pergunta do cliente e a resposta que ele marcou como útil, com as
    // mensagens anteriores da conversa para o modelo aprender a manter o assunto.
    private List<Exemplo> deFeedback(OffsetDateTime ate, Map<Long, List<ItemComprado>> comprasPorCliente,
                                     Function<Long, Optional<PreferenciaCliente>> preferenciaDe,
                                     List<Instrumento> catalogo) {
        List<FeedbackResposta> uteis = feedbackRepository.todosParaTreinamento().stream()
                .filter(f -> f.getAvaliacao() == AvaliacaoResposta.UTIL)
                .filter(f -> !avaliadoEm(f).isAfter(ate))
                .toList();
        if (uteis.isEmpty()) {
            return List.of();
        }

        Set<Long> conversas = uteis.stream().map(f -> f.getMensagem().getConversa().getId()).collect(Collectors.toSet());
        Map<Long, List<MensagemChat>> mensagensPorConversa = mensagemRepository
                .findByConversaIdInOrderByConversaIdAscCriadoEmAscIdAsc(conversas).stream()
                .collect(Collectors.groupingBy(m -> m.getConversa().getId()));

        List<Exemplo> exemplos = new ArrayList<>();
        for (FeedbackResposta feedback : uteis) {
            MensagemChat resposta = feedback.getMensagem();
            List<MensagemChat> conversa = mensagensPorConversa.getOrDefault(resposta.getConversa().getId(), List.of());
            int posicao = conversa.indexOf(resposta);
            if (posicao < 1 || conversa.get(posicao - 1).ehDoAssistente()) {
                continue; // sem pergunta do cliente logo antes, não há o que ensinar
            }

            // Mesmo contexto do chat, inclusive os detalhes dos produtos que a pergunta cita.
            Long clienteId = resposta.getConversa().getCliente().getId();
            String pergunta = conversa.get(posicao - 1).getTexto();
            List<MensagemIa> mensagens = new ArrayList<>();
            mensagens.add(MensagemIa.sistema(contextoService.instrucoes(
                    preferenciaDe.apply(clienteId), comprasPorCliente.getOrDefault(clienteId, List.of()), catalogo)
                    + contextoService.secaoDetalhes(contextoService.citadosNaPergunta(pergunta, catalogo))));
            conversa.subList(Math.max(0, posicao - 1 - MENSAGENS_ANTERIORES), posicao).forEach(m ->
                    mensagens.add(m.ehDoAssistente() ? MensagemIa.assistente(m.getTexto()) : MensagemIa.usuario(m.getTexto())));
            mensagens.add(MensagemIa.assistente(resposta.getTexto()));
            exemplos.add(new Exemplo(Origem.FEEDBACK, mensagens));
        }
        return exemplos;
    }

    // Três perguntas por produto, respondidas com a descrição. A primeira vem
    // sem os detalhes no contexto (o modelo guarda o conteúdo); as outras duas
    // com a seção de detalhes, como o chat envia (o modelo aprende a usá-la).
    private List<Exemplo> doCatalogo(List<Instrumento> catalogo) {
        String sistema = contextoService.instrucoes(Optional.empty(), List.of(), catalogo);
        List<Exemplo> exemplos = new ArrayList<>();
        for (Instrumento produto : catalogo) {
            String nome = artigo(produto) + " " + produto.getNome();
            String detalhes = contextoService.secaoDetalhes(List.of(produto));
            exemplos.add(deCatalogo(sistema, "O que você sabe sobre " + nome + "?", produto));
            exemplos.add(deCatalogo(sistema + detalhes, "Para que serve " + nome + "?", produto));
            exemplos.add(deCatalogo(sistema + detalhes, "Me fale sobre " + nome + ".", produto));
        }
        return exemplos;
    }

    private static Exemplo deCatalogo(String sistema, String pergunta, Instrumento produto) {
        return new Exemplo(Origem.CATALOGO, List.of(
                MensagemIa.sistema(sistema),
                MensagemIa.usuario(pergunta),
                MensagemIa.assistente(descricaoCompleta(produto))));
    }

    private static String pergunta(int indice, Instrumento produto) {
        String modelo = PERGUNTAS.get(indice % PERGUNTAS.size());
        return modelo.contains("%s") ? modelo.formatted(categoriaPrincipal(produto).toLowerCase(PT_BR)) : modelo;
    }

    private static String recomendacao(Instrumento produto, List<ItemComprado> antes) {
        StringBuilder texto = new StringBuilder("Recomendo %s %s [%s], por %s. %s".formatted(
                artigo(produto), produto.getNome(), produto.getCodigo(), moeda(produto.getValorVenda()), resumo(produto)));
        antes.stream().max(Comparator.comparing(ItemComprado::getValorUnitario))
                .ifPresent(maior -> texto.append(" Combina com o que você já tem, como ").append(maior.getNome()).append('.'));
        return texto.toString();
    }

    private static String descricaoCompleta(Instrumento produto) {
        Integer estoque = produto.getQuantidadeEstoque();
        String disponibilidade = estoque != null && estoque > 0 ? "" : " Está esgotado no momento.";
        String artigo = artigo(produto);
        return "%s %s [%s] custa %s.%s %s".formatted(
                Character.toUpperCase(artigo.charAt(0)) + artigo.substring(1), produto.getNome(), produto.getCodigo(),
                moeda(produto.getValorVenda()), disponibilidade, descricao(produto));
    }

    // Concordância pela primeira palavra do nome: "a Guitarra", "as Palhetas", "o Pedal".
    private static String artigo(Instrumento produto) {
        String primeira = produto.getNome().split("\\s+")[0].toLowerCase(PT_BR);
        return switch (primeira) {
            case "guitarra", "interface", "bateria", "caixa" -> "a";
            case "palhetas", "cordas" -> "as";
            default -> "o";
        };
    }

    // As duas primeiras frases da descrição bastam para justificar a recomendação.
    private static String resumo(Instrumento produto) {
        String[] frases = descricao(produto).split("(?<=\\.)\\s+");
        return frases.length <= 2 ? descricao(produto) : frases[0] + " " + frases[1];
    }

    private static String descricao(Instrumento produto) {
        return produto.getDescricao() == null ? "" : produto.getDescricao().trim();
    }

    private static String categoriaPrincipal(Instrumento produto) {
        return produto.getCategorias().stream().map(Categoria::getNome).sorted().findFirst().orElse("produto");
    }

    private static OffsetDateTime avaliadoEm(FeedbackResposta feedback) {
        return feedback.getAtualizadoEm() != null ? feedback.getAtualizadoEm() : feedback.getCriadoEm();
    }

    private static String moeda(BigDecimal valor) {
        return String.format(PT_BR, "R$ %,.2f", valor);
    }

    private static String papel(MensagemIa.Papel papel) {
        return switch (papel) {
            case SISTEMA -> "system";
            case USUARIO -> "user";
            case ASSISTENTE -> "assistant";
        };
    }
}
