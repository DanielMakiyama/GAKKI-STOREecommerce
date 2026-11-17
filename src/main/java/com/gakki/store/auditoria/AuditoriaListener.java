package com.gakki.store.auditoria;

import com.gakki.store.domain.auditoria.OperacaoAuditoria;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostUpdate;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Map;

// Ouve as escritas das entidades anotadas com @EntityListeners e publica
// um evento por operação (RNF0012).
@Component
@RequiredArgsConstructor
public class AuditoriaListener {

    private final ApplicationEventPublisher publicador;

    @PostPersist
    public void aoInserir(Object entidade) {
        publicar(entidade, OperacaoAuditoria.INSERCAO);
    }

    @PostUpdate
    public void aoAlterar(Object entidade) {
        publicar(entidade, OperacaoAuditoria.ALTERACAO);
    }

    private void publicar(Object entidade, OperacaoAuditoria operacao) {
        Map<String, Object> estado = ExtratorDeEstado.extrair(entidade);

        publicador.publishEvent(new EventoDeAuditoria(
                Hibernate.getClass(entidade).getSimpleName(),
                String.valueOf(estado.get("id")),
                operacao,
                emailDoUsuario(),
                estado));
    }

    // Lido aqui, e não no registrador: o SecurityContext é do thread da
    // requisição, e o log é gravado depois.
    private String emailDoUsuario() {
        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacao == null
                || !autenticacao.isAuthenticated()
                || "anonymousUser".equals(autenticacao.getPrincipal())) {
            return null;
        }
        return autenticacao.getName();
    }
}
