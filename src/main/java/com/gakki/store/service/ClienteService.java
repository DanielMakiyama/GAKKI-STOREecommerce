package com.gakki.store.service;

import com.gakki.store.domain.Cliente;
import com.gakki.store.domain.Usuario;
import com.gakki.store.domain.enums.Papel;
import com.gakki.store.dto.request.RegistrarClienteRequest;
import com.gakki.store.dto.response.ClienteResponse;
import com.gakki.store.exception.ConflitoException;
import com.gakki.store.exception.RegraDeNegocioException;
import com.gakki.store.mapper.ClienteMapper;
import com.gakki.store.repository.ClienteRepository;
import com.gakki.store.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

/**
 * RF0021 — cadastro de cliente.
 */
@Service
@RequiredArgsConstructor
public class ClienteService {

    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final EnderecoService enderecoService;
    private final CartaoService cartaoService;
    private final ClienteMapper clienteMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * Cria usuário, cliente, endereço e — se informado — cartão.
     *
     * <p>Tudo numa transação só. A RN0022 exige que todo cliente tenha
     * endereço de entrega: em duas transações separadas, uma falha na
     * segunda deixaria no banco exatamente o que a regra proíbe. Aqui,
     * se qualquer passo falhar, nada é gravado.
     */
    @Transactional
    public ClienteResponse cadastrar(RegistrarClienteRequest requisicao) {
        // RNF0032 — Bean Validation enxerga um campo por vez e não
        // compara dois; a igualdade só pode ser verificada aqui.
        if (!requisicao.senha().equals(requisicao.confirmacaoSenha())) {
            throw new RegraDeNegocioException("A confirmação de senha não confere.");
        }

        String email = normalizarEmail(requisicao.email());
        String cpf = somenteDigitos(requisicao.cpf());

        if (usuarioRepository.existsByEmail(email)) {
            throw new ConflitoException("Já existe um cadastro com este e-mail.");
        }
        if (clienteRepository.existsByCpf(cpf)) {
            throw new ConflitoException("Já existe um cadastro com este CPF.");
        }

        Usuario usuario = new Usuario(email, passwordEncoder.encode(requisicao.senha()), Papel.CLIENTE);
        usuarioRepository.save(usuario);

        Cliente cliente = clienteMapper.paraEntidade(requisicao, usuario, gerarCodigo());
        cliente.setCpf(cpf);
        clienteRepository.save(cliente);

        // RN0021 e RN0022 — o endereço nasce com o cliente, servindo a
        // entrega e a cobrança.
        enderecoService.criarNoCadastro(cliente, requisicao.endereco());

        // RF0027 — cartão é opcional; nenhuma regra condiciona a
        // existência do cliente a possuir um.
        if (requisicao.cartao() != null) {
            cartaoService.criarNoCadastro(cliente, requisicao.cartao());
        }

        return clienteMapper.paraResponse(cliente);
    }

    /**
     * RNF0035 — código único no formato CLI-0001.
     *
     * <p>O número vem de uma sequence do PostgreSQL. Um contador em
     * memória reiniciaria junto com a aplicação e colidiria entre duas
     * instâncias rodando ao mesmo tempo.
     */
    private String gerarCodigo() {
        return String.format("CLI-%04d", clienteRepository.proximoNumeroDeCodigo());
    }

    /**
     * E-mail sempre em minúsculas.
     *
     * <p>Sem isso, "Daniel@x.com" passaria pela checagem de unicidade
     * contra "daniel@x.com" e criaria duas contas que a pessoa enxerga
     * como a mesma.
     */
    private String normalizarEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String somenteDigitos(String valor) {
        return valor.replaceAll("\\D", "");
    }
}
