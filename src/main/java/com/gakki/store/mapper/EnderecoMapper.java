package com.gakki.store.mapper;

import com.gakki.store.domain.Cliente;
import com.gakki.store.domain.Endereco;
import com.gakki.store.dto.request.EnderecoRequest;
import com.gakki.store.dto.response.EnderecoResponse;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class EnderecoMapper {

    public Endereco paraEntidade(EnderecoRequest requisicao, Cliente cliente) {
        Endereco endereco = new Endereco();
        endereco.setCliente(cliente);
        aplicar(requisicao, endereco);
        endereco.setEntrega(requisicao.entrega());
        endereco.setCobranca(requisicao.cobranca());
        return endereco;
    }

    /**
     * RNF0034 — alteração do endereço sem tocar no resto do cadastro.
     *
     * <p>Não mexe em {@code principal}: essa marcação tem endpoint
     * próprio ({@code PATCH .../principal}), porque mudá-la exige
     * desmarcar o anterior na mesma transação. Deixá-la aqui permitiria
     * criar um segundo principal por um PUT comum, e o índice parcial do
     * banco recusaria a operação.
     *
     * <p>Também não mexe em {@code entrega} e {@code cobranca} na
     * atualização: quem as altera precisa passar pela verificação da
     * RN0021/RN0022, que vive no service.
     */
    public void aplicar(EnderecoRequest requisicao, Endereco endereco) {
        endereco.setApelido(requisicao.apelido().trim());
        endereco.setTipoResidencia(requisicao.tipoResidencia());
        endereco.setTipoLogradouro(requisicao.tipoLogradouro());
        endereco.setLogradouro(requisicao.logradouro().trim());
        endereco.setNumero(requisicao.numero().trim());
        endereco.setComplemento(vazioViraNulo(requisicao.complemento()));
        endereco.setBairro(requisicao.bairro().trim());
        endereco.setCep(Formatos.somenteDigitos(requisicao.cep()));
        endereco.setCidade(requisicao.cidade().trim());
        endereco.setEstado(requisicao.estado().trim().toUpperCase(Locale.ROOT));
        endereco.setPais(requisicao.pais().trim());
        endereco.setObservacoes(vazioViraNulo(requisicao.observacoes()));
    }

    public EnderecoResponse paraResponse(Endereco endereco) {
        return new EnderecoResponse(
                endereco.getId(),
                endereco.getApelido(),
                endereco.getTipoResidencia(),
                endereco.getTipoLogradouro(),
                endereco.getLogradouro(),
                endereco.getNumero(),
                endereco.getComplemento(),
                endereco.getBairro(),
                endereco.getCep(),
                endereco.getCidade(),
                endereco.getEstado(),
                endereco.getPais(),
                endereco.getObservacoes(),
                endereco.isEntrega(),
                endereco.isCobranca(),
                endereco.isPrincipal());
    }

    /**
     * Campo opcional em branco vira NULL.
     *
     * <p>Sem isso o banco acumula string vazia e NULL significando a
     * mesma coisa, e toda consulta precisa testar os dois.
     */
    private String vazioViraNulo(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return valor.trim();
    }
}
