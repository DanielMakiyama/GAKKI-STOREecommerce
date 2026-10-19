package com.gakki.store.service;

import com.gakki.store.domain.cliente.Cartao;
import com.gakki.store.domain.vendas.Pagamento;
import com.gakki.store.domain.vendas.Pedido;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/*
 * RN0037 — retorno da operadora de cartão de crédito.
 *
 * O próprio DRS autoriza a simulação, em nota de rodapé da RN0037:
 * "Isso pode ser SIMULADO através de um método estático ou uma tabela
 * fake". Esta classe é essa simulação, isolada num componente próprio
 * para que o PedidoService dependa de um contrato — "a operadora aceita
 * ou recusa" — e não de um `if` embutido na regra de negócio. Quando a
 * integração real existir, troca-se a implementação e o service não muda.
 *
 * O critério de recusa é o final do cartão, configurável em
 * `gakki.pagamento.digitos-recusados` (padrão 0000). A escolha é
 * deliberada: precisa ser determinística, para a demonstração poder
 * exibir uma compra REPROVADA quando quiser, e precisa ser acionável
 * pela própria interface, porque o cliente cadastra o cartão pela tela.
 * Um sorteio aleatório deixaria o teste de aceitação intermitente, que é
 * o pior tipo de teste.
 *
 * Nenhum dado de cartão trafega aqui além dos quatro últimos dígitos: o
 * modelo não guarda número completo nem código de segurança (ver a
 * divergência consciente da RN0024, registrada no contrato de Vendas).
 */
@Component
public class OperadoraDeCartao {

    private final String digitosRecusados;

    public OperadoraDeCartao(
            @Value("${gakki.pagamento.digitos-recusados:0000}") String digitosRecusados) {
        this.digitosRecusados = digitosRecusados;
    }

    /*
     * Submete à operadora as linhas de cartão do pedido.
     *
     * Basta uma recusa para a compra inteira ser recusada: não existe
     * pagamento pela metade. Um pedido pago só com cupons não tem o que
     * submeter, e é aprovado direto — a validade dos cupons já foi
     * conferida na finalização, pela RN0033 e pela RN0036.
     */
    public Retorno autorizar(Pedido pedido) {
        List<Cartao> cartoes = pedido.getPagamentos().stream()
                .map(Pagamento::getCartao)
                .filter(cartao -> cartao != null)
                .toList();

        return cartoes.stream()
                .filter(cartao -> digitosRecusados.equals(cartao.getUltimosDigitos()))
                .findFirst()
                .map(cartao -> Retorno.recusado(
                        "A operadora recusou o cartão %s (final %s)."
                                .formatted(cartao.getApelido(), cartao.getUltimosDigitos())))
                .orElseGet(Retorno::aceito);
    }

    /*
     * O motivo só existe na recusa. É `record` porque é dado de retorno,
     * imutável, e o nome do campo documenta melhor que um boolean solto
     * no meio do service.
     *
     * A fábrica do caso feliz chama-se `aceito` e não `aprovado`: o
     * componente `aprovado` já gera o acessor `aprovado()`, e um método
     * estático de mesmo nome e mesma aridade colidiria com ele.
     */
    public record Retorno(boolean aprovado, String motivo) {

        public static Retorno aceito() {
            return new Retorno(true, null);
        }

        public static Retorno recusado(String motivo) {
            return new Retorno(false, motivo);
        }
    }
}
