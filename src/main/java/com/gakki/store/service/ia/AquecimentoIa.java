package com.gakki.store.service.ia;

import com.gakki.store.config.IaProperties;
import com.gakki.store.service.AssistenteIaService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

// Aquece a IA quando o backend termina de subir, numa thread separada para
// não atrasar a inicialização. Desligue com gakki.ia.aquecer-na-inicializacao.
@Component
@RequiredArgsConstructor
public class AquecimentoIa {

    private final AssistenteIaService assistenteIaService;
    private final IaProperties propriedades;

    @EventListener(ApplicationReadyEvent.class)
    public void aoIniciar() {
        if (propriedades.aquecerNaInicializacao()) {
            Thread.ofVirtual().name("aquecimento-ia").start(assistenteIaService::aquecer);
        }
    }
}
