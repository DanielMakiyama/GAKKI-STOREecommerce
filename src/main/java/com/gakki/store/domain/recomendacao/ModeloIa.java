package com.gakki.store.domain.recomendacao;

import com.gakki.store.auditoria.AuditoriaListener;
import com.gakki.store.domain.recomendacao.enums.StatusModeloIa;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

// Uma versão do modelo, gerada por um ajuste fino (RNF0044, item 3).
// dados_ate é o corte: vendas e feedback posteriores entram na próxima versão.

@Entity
@EntityListeners(AuditoriaListener.class)
@Table(name = "modelo_ia")
@Getter
@Setter
@NoArgsConstructor
public class ModeloIa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Integer versao;

    @Column(nullable = false, length = 30)
    private String provedor;

    @Column(name = "modelo_base", nullable = false, length = 100)
    private String modeloBase;

    // Nome do modelo ajustado no provedor; só existe depois do treinamento.
    @Column(length = 300)
    private String identificador;

    @Column(name = "job_id", length = 300)
    private String jobId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusModeloIa status;

    @Column(nullable = false)
    private boolean ativo;

    @Column(name = "exemplos_total", nullable = false)
    private int exemplosTotal;

    @Column(name = "exemplos_vendas", nullable = false)
    private int exemplosVendas;

    @Column(name = "exemplos_feedback", nullable = false)
    private int exemplosFeedback;

    @Column(name = "dados_ate", nullable = false)
    private OffsetDateTime dadosAte;

    @CreationTimestamp
    @Column(name = "iniciado_em", nullable = false, updatable = false)
    private OffsetDateTime iniciadoEm;

    @Column(name = "concluido_em")
    private OffsetDateTime concluidoEm;

    @Column(length = 1000)
    private String erro;

    public void iniciarTreinamento(String jobId) {
        this.jobId = jobId;
        this.status = StatusModeloIa.TREINANDO;
    }

    public void concluir(String identificador) {
        this.identificador = identificador;
        this.status = StatusModeloIa.PRONTO;
        this.concluidoEm = OffsetDateTime.now();
    }

    public void falhar(String motivo) {
        this.erro = motivo == null || motivo.length() <= 1000 ? motivo : motivo.substring(0, 1000);
        this.status = StatusModeloIa.FALHOU;
        this.concluidoEm = OffsetDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ModeloIa outro)) {
            return false;
        }
        return id != null && id.equals(outro.getId());
    }

    @Override
    public int hashCode() {
        return ModeloIa.class.hashCode();
    }
}
