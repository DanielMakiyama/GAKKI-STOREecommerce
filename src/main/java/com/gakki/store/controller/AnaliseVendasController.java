package com.gakki.store.controller;

import com.gakki.store.dto.response.HistoricoVendasResponse;
import com.gakki.store.service.AnaliseVendasService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/*
 * RF0055 — análise do histórico de vendas por categoria.
 *
 * Exclusivo do administrador (critério b): o @PreAuthorize fica na
 * classe, e não no método, para que um endpoint novo da análise já nasça
 * protegido. Sem token a resposta é 401 (o SecurityConfig exige
 * autenticação); com token de cliente, 403.
 *
 * A rota é a que o client.js do front já chama, `/analise/vendas`.
 */
@RestController
@RequestMapping("/analise")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AnaliseVendasController {

    private final AnaliseVendasService analiseVendasService;

    /*
     * GET /analise/vendas?inicio=2026-04-01&fim=2026-09-30&categorias=Guitarras&categorias=Pedais
     *
     * Datas no formato ISO (AAAA-MM-DD), o que o <input type="date">
     * produz. O formato é declarado no @DateTimeFormat porque, sem ele, o
     * Spring tenta o formato curto do idioma do servidor (9/30/26), e a
     * mesma URL funcionaria numa máquina e não noutra.
     *
     * Os três parâmetros são opcionais aqui de propósito: quem confere se
     * vieram é o service, que devolve 400 com a mensagem em português
     * ("Selecione ao menos uma categoria..."). Se fossem obrigatórios, o
     * Spring recusaria antes, com a mensagem padrão dele, em inglês, e a
     * regra ficaria espalhada em dois lugares.
     *
     * Sem paginação, embora seja uma listagem: o resultado é a série de um
     * gráfico, de tamanho fixo (categorias × meses do período). Cortá-la em
     * páginas desenharia só um pedaço das linhas.
     */
    @GetMapping("/vendas")
    public ResponseEntity<List<HistoricoVendasResponse>> historicoDeVendas(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam(required = false) List<String> categorias) {

        return ResponseEntity.ok(analiseVendasService.historico(inicio, fim, categorias));
    }
}
