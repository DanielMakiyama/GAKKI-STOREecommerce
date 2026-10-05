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
//
// NÃO grava no banco aqui, de propósito. Estes callbacks rodam dentro do
// flush do Hibernate, e persistir uma entidade nova durante o flush mexe
// no contexto de persistência enquanto ele está sendo percorrido — falha
// de forma intermitente. Quem grava é o RegistradorDeAuditoria, depois do
// commit (contrato de Vendas v3, decisão 8).
//
// @PostPersist, e não @PrePersist: com GenerationType.IDENTITY o id só
// existe depois do INSERT. No @PrePersist, getId() ainda devolve null — e
// o log sairia sem saber a qual registro se refere.
//
// É um bean do Spring: o Spring Boot registra o SpringBeanContainer no
// Hibernate, então o listener recebe injeção normalmente.

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
