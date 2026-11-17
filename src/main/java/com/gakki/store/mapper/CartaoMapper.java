package com.gakki.store.mapper;

import com.gakki.store.domain.cliente.Bandeira;
import com.gakki.store.domain.cliente.Cartao;
import com.gakki.store.domain.cliente.Cliente;
import com.gakki.store.dto.request.CartaoRequest;
import com.gakki.store.dto.response.BandeiraResponse;
import com.gakki.store.dto.response.CartaoResponse;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class CartaoMapper {


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
