package com.gakki.store.auditoria;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gakki.store.domain.auditoria.LogTransacao;
import com.gakki.store.repository.LogTransacaoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Map;

// Grava a linha de log_transacao (RNF0012).
@Slf4j
@Component
@RequiredArgsConstructor
public class RegistradorDeAuditoria {

    private final LogTransacaoRepository logTransacaoRepository;
    private final ObjectMapper json;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrar(EventoDeAuditoria evento) {
        try {
            LogTransacao log = new LogTransacao();
            log.setEntidade(evento.entidade());
            log.setEntidadeId(evento.entidadeId());
            log.setOperacao(evento.operacao());
            log.setUsuarioEmail(evento.usuarioEmail());
            log.setDadosAlterados(serializar(evento.dados()));

            logTransacaoRepository.save(log);
        } catch (RuntimeException e) {
            // A operação de negócio já foi confirmada. Falhar aqui não
            // pode desfazer nem mascarar o que o cliente já viu dar certo:
            // registra no log da aplicação e segue.
            log.error("Falha ao registrar auditoria de {} {}",
                    evento.entidade(), evento.entidadeId(), e);
        }
    }

    private String serializar(Map<String, Object> dados) {
        try {
            return json.writeValueAsString(dados);
        } catch (Exception e) {
            log.warn("Falha ao serializar dados de auditoria", e);
            return "{\"erro\":\"falha ao serializar o estado da entidade\"}";
        }
    }
}
