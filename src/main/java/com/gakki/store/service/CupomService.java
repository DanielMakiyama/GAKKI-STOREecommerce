package com.gakki.store.service;

import com.gakki.store.domain.cliente.Cliente;
import com.gakki.store.domain.vendas.Cupom;
import com.gakki.store.domain.vendas.Pedido;
import com.gakki.store.domain.vendas.enums.TipoCupom;
import com.gakki.store.dto.response.CupomResponse;
import com.gakki.store.exception.RecursoNaoEncontradoException;
import com.gakki.store.exception.RegraDeNegocioException;
import com.gakki.store.mapper.CupomMapper;
import com.gakki.store.repository.ClienteRepository;
import com.gakki.store.repository.CupomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

/*
 * RF0037 — cupons como forma de pagamento, com RN0033 e RN0036.
 *
 * A RN0035 não está aqui: ela descreve a interação entre cupom e cartão
 * (o piso de R$10 cai quando há cupom), e isso só se decide onde as duas
 * formas de pagamento se encontram, no PedidoService.
 *
 * Os métodos chamados durante a finalização não têm @Transactional
 * próprio: participam da transação do pedido, que precisa ser atômica
 * (RF0038). Marcar REQUIRES_NEW aqui faria o cupom ser consumido mesmo
 * se a gravação do pedido falhasse depois.
 */
@Service
@RequiredArgsConstructor
public class CupomService {

    private static final String PREFIXO_TROCA = "TROCA";

    private final CupomRepository cupomRepository;
    private final ClienteRepository clienteRepository;
    private final CupomMapper cupomMapper;

    @Transactional(readOnly = true)
    public List<CupomResponse> listarMeusCupons(String email) {
        return cupomRepository
                .findByClienteIdOrderByUtilizadoAscValorDesc(clienteDoEmail(email).getId())
                .stream()
                .map(cupomMapper::paraResponse)
                .toList();
    }

    /*
     * Resolve os códigos escolhidos no checkout e aplica as regras de
     * seleção. Devolve os cupons já validados, prontos para virar linhas
     * de pagamento.
     *
     * `valorCompra` é o total do pedido com frete: a RN0036 compara a
     * cobertura dos cupons contra o que de fato será pago.
     */
    public List<Cupom> validarSelecao(Cliente cliente, List<String> codigos, BigDecimal valorCompra) {
        if (codigos == null || codigos.isEmpty()) {
            return List.of();
        }

        List<String> solicitados = codigos.stream()
                .filter(codigo -> codigo != null && !codigo.isBlank())
                .map(String::trim)
                .toList();

        // Repetir o mesmo código seria uma forma de usar o valor duas
        // vezes. Recusar é mais honesto que deduplicar em silêncio: o
        // cliente precisa saber que a seleção dele estava errada.
        if (solicitados.stream().distinct().count() != solicitados.size()) {
            throw new RegraDeNegocioException("Há cupom repetido na seleção.");
        }

        List<Cupom> cupons = cupomRepository
                .findByClienteIdAndCodigoIn(cliente.getId(), solicitados);

        // Mensagem deliberadamente genérica: distinguir "não existe" de
        // "não é seu" transformaria o checkout num testador de códigos
        // alheios.
        if (cupons.size() != solicitados.size()) {
            throw new RegraDeNegocioException(
                    "Cupom inválido ou não disponível para este cliente.");
        }

        cupons.stream()
                .filter(Cupom::isUtilizado)
                .findFirst()
                .ifPresent(cupom -> {
                    throw new RegraDeNegocioException(
                            "O cupom %s já foi utilizado.".formatted(cupom.getCodigo()));
                });

        validarUmPromocional(cupons);
        validarSelecaoMinima(cupons, valorCompra);

        return cupons;
    }

    /** RN0033 — apenas um cupom promocional por compra. */
    private void validarUmPromocional(List<Cupom> cupons) {
        long promocionais = cupons.stream()
                .filter(cupom -> cupom.getTipo() == TipoCupom.PROMOCIONAL)
                .count();

        if (promocionais > 1) {
            throw new RegraDeNegocioException(
                    "É permitido apenas um cupom promocional por compra.");
        }
    }

    /*
     * RN0036 — não é permitido usar cupom desnecessário.
     *
     * O DRS não formaliza a regra, só dá um exemplo numérico. A regra
     * adotada (contrato de Vendas v3, decisão 4) é a de conjunto mínimo:
     *
     *     soma(selecionados) − menor(selecionados) < valorCompra
     *
     * Em palavras: tirar o menor cupom da seleção precisa derrubar a
     * cobertura da compra. Se a compra continuar coberta sem ele, aquele
     * cupom estava sobrando e seria queimado à toa.
     *
     * Com cupons de R$20, R$40 e R$35 numa compra de R$50 — o exemplo do
     * DRS — os três pares passam (trocos de R$10, R$5 e R$25) e o
     * conjunto com os três é recusado: 95 − 20 = 75, que já cobre os 50.
     *
     * Um cupom só nunca é desnecessário: não há o que remover.
     */
    private void validarSelecaoMinima(List<Cupom> cupons, BigDecimal valorCompra) {
        if (cupons.size() < 2) {
            return;
        }

        BigDecimal soma = somaDe(cupons);
        BigDecimal menor = cupons.stream()
                .map(Cupom::getValor)
                .min(BigDecimal::compareTo)
                .orElseThrow();

        if (soma.subtract(menor).compareTo(valorCompra) >= 0) {
            throw new RegraDeNegocioException(
                    ("A seleção tem cupom desnecessário: a compra de R$ %s já estaria coberta "
                            + "sem o cupom de R$ %s.").formatted(valorCompra, menor));
        }
    }

    public BigDecimal somaDe(List<Cupom> cupons) {
        return cupons.stream()
                .map(Cupom::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /*
     * RN0036 — quando os cupons cobrem mais do que a compra, a diferença
     * volta como cupom de troca novo.
     *
     * Sem @Transactional próprio: participa da transação do pedido. Se a
     * finalização falhar depois disto, o cupom de troco não pode
     * sobreviver sozinho.
     */
    public Optional<Cupom> gerarTrocoSeNecessario(Cliente cliente,
                                                  BigDecimal valorEmCupons,
                                                  BigDecimal valorCompra) {

        BigDecimal sobra = valorEmCupons.subtract(valorCompra);
        if (sobra.compareTo(BigDecimal.ZERO) <= 0) {
            return Optional.empty();
        }

        Cupom troco = new Cupom();
        troco.setCliente(cliente);
        troco.setTipo(TipoCupom.TROCA);
        troco.setValor(sobra.setScale(2, RoundingMode.HALF_UP));
        troco.setCodigo(proximoCodigoDeTroca());

        return Optional.of(cupomRepository.save(troco));
    }

    /*
     * Marca os cupons como consumidos e amarra cada um ao pedido.
     *
     * Na mesma transação da gravação do pedido: ou o pedido existe e os
     * cupons estão gastos, ou nada aconteceu. Um cupom gasto sem pedido
     * seria dinheiro desaparecido.
     */
    public void marcarComoUtilizados(List<Cupom> cupons, Pedido pedido) {
        cupons.forEach(cupom -> {
            cupom.setUtilizado(true);
            cupom.setPedidoUtilizacao(pedido);
        });
    }

    private String proximoCodigoDeTroca() {
        String codigo = "%s-%04d".formatted(PREFIXO_TROCA, cupomRepository.proximoNumeroDeCupom());

        // A sequence começa em 1000, acima dos códigos fixos do seed, então
        // colidir é impossível. A conferência fica porque código de cupom
        // vira dinheiro: se um dia alguém reiniciar a sequence, é melhor
        // falhar aqui do que emitir um código que já pertence a outro
        // cliente.
        if (cupomRepository.existsByCodigo(codigo)) {
            throw new RegraDeNegocioException(
                    "Não foi possível gerar o cupom de troco: código %s já existe.".formatted(codigo));
        }
        return codigo;
    }

    private Cliente clienteDoEmail(String email) {
        return clienteRepository.findByUsuarioEmail(email)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Nenhum cadastro de cliente associado a este usuário."));
    }
}
