package com.gakki.store.mapper;

import com.gakki.store.domain.Cliente;
import com.gakki.store.domain.Usuario;
import com.gakki.store.dto.request.AtualizarClienteRequest;
import com.gakki.store.dto.request.RegistrarClienteRequest;
import com.gakki.store.dto.request.TelefoneRequest;
import com.gakki.store.dto.response.ClienteResponse;
import com.gakki.store.dto.response.ClienteResumoResponse;
import com.gakki.store.dto.response.TelefoneResponse;
import org.springframework.stereotype.Component;

@Component
public class ClienteMapper {

    /**
     * Monta a entidade a partir do cadastro.
     *
     * <p>Recebe o {@link Usuario} e o código já prontos porque os dois
     * dependem de coisas que o mapper não deve conhecer: o hash da senha
     * vem do PasswordEncoder e o código vem de uma sequence do banco.
     * Mapper converte formato; quem decide valor é o service.
     */
    public Cliente paraEntidade(RegistrarClienteRequest requisicao, Usuario usuario, String codigo) {
        Cliente cliente = new Cliente();
        cliente.setUsuario(usuario);
        cliente.setCodigo(codigo);
        cliente.setNome(requisicao.nome().trim());
        cliente.setCpf(Formatos.somenteDigitos(requisicao.cpf()));
        cliente.setGenero(requisicao.genero());
        cliente.setDataNascimento(requisicao.dataNascimento());
        aplicarTelefone(requisicao.telefone(), cliente);
        return cliente;
    }

    /**
     * RF0022 — aplica os campos livres da alteração.
     *
     * <p>E-mail e CPF ficam de fora de propósito, embora o DTO os
     * traga: os dois são únicos no banco, e quem precisa comparar com o
     * valor atual e devolver 409 é o service. O mapper converte formato;
     * decidir se a mudança é permitida não é trabalho dele.
     */
    public void aplicar(AtualizarClienteRequest requisicao, Cliente cliente) {
        cliente.setNome(requisicao.nome().trim());
        cliente.setGenero(requisicao.genero());
        cliente.setDataNascimento(requisicao.dataNascimento());
        aplicarTelefone(requisicao.telefone(), cliente);
    }

    /**
     * Cadastro visto pelo próprio dono, com o CPF por inteiro.
     *
     * <p>A máscara existe para que o CPF de um cliente não circule em
     * tela de administrador, log de navegador ou captura de tela alheia.
     * Contra o próprio titular ela não protege nada — ele sabe o próprio
     * CPF — e atrapalha: desde que a RF0022 passou a permitir corrigi-lo,
     * um formulário preenchido com {@code ***.456.789-**} gravaria a
     * máscara de volta no banco.
     */
    public ClienteResponse paraResponseDoDono(Cliente cliente) {
        return montar(cliente, cliente.getCpf());
    }

    //Cadastro visto por terceiros (admin): CPF mascarado
    public ClienteResponse paraResponse(Cliente cliente) {
        return montar(cliente, Formatos.mascararCpf(cliente.getCpf()));
    }

    private ClienteResponse montar(Cliente cliente, String cpf) {
        Usuario usuario = cliente.getUsuario();
        return new ClienteResponse(
                cliente.getId(),
                cliente.getCodigo(),
                cliente.getNome(),
                usuario.getEmail(),
                cpf,
                cliente.getGenero(),
                cliente.getDataNascimento(),
                new TelefoneResponse(
                        cliente.getTelefoneTipo(),
                        cliente.getTelefoneDdd(),
                        cliente.getTelefoneNumero()),
                usuario.getPapel(),
                usuario.isAtivo());
    }

   //Linha da listagem administrativa (RF0024)
    public ClienteResumoResponse paraResumo(Cliente cliente) {
        Usuario usuario = cliente.getUsuario();
        return new ClienteResumoResponse(
                cliente.getId(),
                cliente.getCodigo(),
                cliente.getNome(),
                usuario.getEmail(),
                Formatos.mascararCpf(cliente.getCpf()),
                usuario.isAtivo());
    }

    private void aplicarTelefone(TelefoneRequest telefone, Cliente cliente) {
        cliente.setTelefoneTipo(telefone.tipo());
        cliente.setTelefoneDdd(Formatos.somenteDigitos(telefone.ddd()));
        cliente.setTelefoneNumero(Formatos.somenteDigitos(telefone.numero()));
    }
}
