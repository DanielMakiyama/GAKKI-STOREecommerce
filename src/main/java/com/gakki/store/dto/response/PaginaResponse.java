package com.gakki.store.dto.response;

import org.springframework.data.domain.Page;

import java.util.List;

// Envelope de paginação da API.
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
