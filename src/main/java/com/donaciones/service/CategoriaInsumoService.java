package com.donaciones.service;

import com.donaciones.dto.request.CategoriaInsumoRequest;
import com.donaciones.dto.response.CategoriaInsumoResponse;
import com.donaciones.entity.CategoriaInsumo;
import com.donaciones.exception.DuplicateResourceException;
import com.donaciones.exception.ResourceNotFoundException;
import com.donaciones.repository.CategoriaInsumoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoriaInsumoService {

    private final CategoriaInsumoRepository categoriaRepository;

    @Transactional(readOnly = true)
    public List<CategoriaInsumoResponse> list() {
        return categoriaRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoriaInsumoResponse getById(Integer id) {
        return toResponse(findByIdOrThrow(id));
    }

    @Transactional
    public CategoriaInsumoResponse create(CategoriaInsumoRequest request) {
        if (categoriaRepository.existsByNombreCategoria(request.getNombreCategoria())) {
            throw new DuplicateResourceException(
                    "Ya existe una categoría con el nombre " + request.getNombreCategoria());
        }
        CategoriaInsumo categoria = CategoriaInsumo.builder()
                .nombreCategoria(request.getNombreCategoria())
                .unidadMedCate(request.getUnidadMedCate())
                .refrigerar(request.getRefrigerar() != null ? request.getRefrigerar() : false)
                .build();
        return toResponse(categoriaRepository.save(categoria));
    }

    @Transactional
    public CategoriaInsumoResponse update(Integer id, CategoriaInsumoRequest request) {
        CategoriaInsumo categoria = findByIdOrThrow(id);
        if (categoriaRepository.existsByNombreCategoriaAndIdCategoriaNot(request.getNombreCategoria(), id)) {
            throw new DuplicateResourceException(
                    "Ya existe una categoría con el nombre " + request.getNombreCategoria());
        }
        categoria.setNombreCategoria(request.getNombreCategoria());
        categoria.setUnidadMedCate(request.getUnidadMedCate());
        if (request.getRefrigerar() != null) {
            categoria.setRefrigerar(request.getRefrigerar());
        }
        return toResponse(categoriaRepository.save(categoria));
    }

    @Transactional
    public void delete(Integer id) {
        CategoriaInsumo categoria = findByIdOrThrow(id);
        categoriaRepository.delete(categoria);
    }

    private CategoriaInsumo findByIdOrThrow(Integer id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la categoría con id " + id));
    }

    private CategoriaInsumoResponse toResponse(CategoriaInsumo categoria) {
        return CategoriaInsumoResponse.builder()
                .idCategoria(categoria.getIdCategoria())
                .nombreCategoria(categoria.getNombreCategoria())
                .unidadMedCate(categoria.getUnidadMedCate())
                .refrigerar(categoria.getRefrigerar())
                .build();
    }

}