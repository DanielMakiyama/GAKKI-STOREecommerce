package com.gakki.store.repository;

import com.gakki.store.domain.vendas.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

/*
 * Categorias de instrumento (RN0012), tabela de domínio.
 *
 * Sem paginação nas duas consultas, como a de bandeiras: é uma lista
 * fechada e pequena, populada por migration, que serve para montar
 * seletores. Paginá-la só obrigaria a tela a pedir "todas as páginas".
 */
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    // Opções da tela de análise de vendas (RF0055), em ordem alfabética
    // para a lista de seleção não mudar de ordem entre uma visita e outra.
    List<Categoria> findAllByOrderByNomeAsc();

    // Resolve os nomes que a tela envia. O front escolhe a categoria pelo
    // nome (é o que o mock já fazia); o service compara o tamanho da
    // lista devolvida com o da pedida para recusar nome inexistente.
    List<Categoria> findByNomeIn(Collection<String> nomes);
}
