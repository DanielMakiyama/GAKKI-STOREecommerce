package com.gakki.store.mapper;

import com.gakki.store.domain.Bandeira;
import com.gakki.store.domain.Cartao;
import com.gakki.store.domain.Cliente;
import com.gakki.store.dto.request.CartaoRequest;
import com.gakki.store.dto.response.BandeiraResponse;
import com.gakki.store.dto.response.CartaoResponse;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class CartaoMapper {

    /**
     * A bandeira chega resolvida como entidade, e não como id.
     *
     * <p>Quem a busca é o service, que aproveita para conferir se ela
     * existe e está ativa (RN0025). Se o mapper recebesse o id e fosse
     * buscar sozinho, precisaria de um repository — e mapper com acesso
     * a banco deixa de ser conversor e vira mais uma camada de regra.
     */
    public Cartao paraEntidade(CartaoRequest requisicao, Cliente cliente, Bandeira bandeira) {
        Cartao cartao = new Cartao();
        cartao.setCliente(cliente);
        aplicar(requisicao, cartao, bandeira);
        return cartao;
    }

    public void aplicar(CartaoRequest requisicao, Cartao cartao, Bandeira bandeira) {
        cartao.setApelido(requisicao.apelido().trim());
        cartao.setUltimosDigitos(Formatos.somenteDigitos(requisicao.ultimosDigitos()));
        cartao.setBandeira(bandeira);
        // Nome impresso em cartão é sempre em maiúsculas — normalizar
        // aqui evita a mesma lista exibir "DANIEL" e "Daniel".
        cartao.setNomeTitular(requisicao.nomeTitular().trim().toUpperCase(Locale.ROOT));
        cartao.setValidadeMes(requisicao.validadeMes());
        cartao.setValidadeAno(requisicao.validadeAno());
    }

    public CartaoResponse paraResponse(Cartao cartao) {
        return new CartaoResponse(
                cartao.getId(),
                cartao.getApelido(),
                cartao.getUltimosDigitos(),
                cartao.getBandeira().getNome(),
                cartao.getNomeTitular(),
                cartao.getValidadeMes(),
                cartao.getValidadeAno(),
                cartao.isPreferencial());
    }

    public BandeiraResponse paraResponse(Bandeira bandeira) {
        return new BandeiraResponse(bandeira.getId(), bandeira.getNome());
    }
}
