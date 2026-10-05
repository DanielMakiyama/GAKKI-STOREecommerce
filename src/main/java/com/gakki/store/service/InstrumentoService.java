package com.gakki.store.service;

import com.gakki.store.dto.response.InstrumentoResponse;
import com.gakki.store.dto.response.PaginaResponse;
import com.gakki.store.exception.RecursoNaoEncontradoException;
import com.gakki.store.mapper.InstrumentoMapper;
import com.gakki.store.repository.InstrumentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Catálogo (RF0011), somente leitura.
//
// O cadastro e a inativação de instrumento (RF0012, RF0051) são de outra
// fatia: o enunciado desta atividade pressupõe que o catálogo e o estoque
// já existam na base. Por isso aqui não há adicionar, alterar nem remover.

@Service
@RequiredArgsConstructor
public class InstrumentoService {

    private final InstrumentoRepository instrumentoRepository;
    private final InstrumentoMapper instrumentoMapper;

    @Transactional(readOnly = true)
    public PaginaResponse<InstrumentoResponse> listar(Pageable paginacao) {
        return PaginaResponse.de(
                instrumentoRepository.findByAtivoTrue(paginacao)
                        .map(instrumentoMapper::paraResponse));
    }

    @Transactional(readOnly = true)
    public InstrumentoResponse buscarPorId(Long id) {
        return instrumentoRepository.findDetalhadoByIdAndAtivoTrue(id)
                .map(instrumentoMapper::paraResponse)
                .orElseThrow(() -> RecursoNaoEncontradoException.instrumento(id));
    }
}
