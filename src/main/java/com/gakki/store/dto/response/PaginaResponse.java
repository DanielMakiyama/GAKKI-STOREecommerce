package com.gakki.store.dto.response;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Envelope de paginação da API.
 *
 * <p>Os nomes dos campos espelham exatamente o que o front já consome
 * (a função {@code paginar()} do mock devolve este formato), então a
 * troca do mock pelo backend real não mexe em nenhuma tela.
 *
 * <p>Existe em vez de serializar o {@code Page} do Spring Data direto
 * por dois motivos: o Spring Boot avisa que o JSON do {@code PageImpl}
 * não é estável entre versões, e serializá-lo entregaria campos
 * internos ({@code pageable}, {@code sort}, {@code first}, {@code last})
 * que nenhuma tela usa.
 */
public record PaginaResponse<T>(
        List<T> content,
        long totalElements,
        int totalPages,
        int number,
        int size) {

    public static <T> PaginaResponse<T> de(Page<T> pagina) {
        return new PaginaResponse<>(
                pagina.getContent(),
                pagina.getTotalElements(),
                pagina.getTotalPages(),
                pagina.getNumber(),
                pagina.getSize());
    }
}
