package com.donaciones.controller;

import com.donaciones.dto.ApiResponseDTO;
import com.donaciones.dto.request.CategoriaInsumoRequest;
import com.donaciones.dto.response.CategoriaInsumoResponse;
import com.donaciones.service.CategoriaInsumoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categorias")
@RequiredArgsConstructor
@Tag(name = "Categorías de Insumos", description = "Gestión de categorías de productos (alimentos, medicinas, etc.)")
public class CategoriaInsumoController {

    private final CategoriaInsumoService categoriaService;

    @GetMapping
    @Operation(summary = "Listar categorías", description = "Devuelve todas las categorías de insumos, incluidas las deshabilitadas.")
    public ResponseEntity<ApiResponseDTO<List<CategoriaInsumoResponse>>> list(HttpServletRequest servletRequest) {
        List<CategoriaInsumoResponse> categorias = categoriaService.list();
        ApiResponseDTO<List<CategoriaInsumoResponse>> body = ApiResponseDTO.success(HttpStatus.OK,
                "Categorías obtenidas exitosamente", servletRequest.getRequestURI(), categorias);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/activas")
    @Operation(summary = "Listar categorías activas",
            description = "Devuelve solo las categorías activas, para poblar el desplegable al registrar un producto.")
    public ResponseEntity<ApiResponseDTO<List<CategoriaInsumoResponse>>> listActive(HttpServletRequest servletRequest) {
        List<CategoriaInsumoResponse> categorias = categoriaService.listActive();
        ApiResponseDTO<List<CategoriaInsumoResponse>> body = ApiResponseDTO.success(HttpStatus.OK,
                "Categorías activas obtenidas exitosamente", servletRequest.getRequestURI(), categorias);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener categoría por ID", description = "Devuelve el detalle de una categoría.")
    public ResponseEntity<ApiResponseDTO<CategoriaInsumoResponse>> getById(@PathVariable Integer id,
                                                                           HttpServletRequest servletRequest) {
        CategoriaInsumoResponse categoria = categoriaService.getById(id);
        ApiResponseDTO<CategoriaInsumoResponse> body = ApiResponseDTO.success(HttpStatus.OK,
                "Categoría obtenida exitosamente", servletRequest.getRequestURI(), categoria);
        return ResponseEntity.ok(body);
    }

    @PostMapping
    @Operation(summary = "Registrar categoría", description = "Registra una nueva categoría de insumo.")
    public ResponseEntity<ApiResponseDTO<CategoriaInsumoResponse>> create(@Valid @RequestBody CategoriaInsumoRequest request,
                                                                          HttpServletRequest servletRequest) {
        CategoriaInsumoResponse categoria = categoriaService.create(request);
        ApiResponseDTO<CategoriaInsumoResponse> body = ApiResponseDTO.success(HttpStatus.CREATED,
                "Categoría registrada exitosamente", servletRequest.getRequestURI(), categoria);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar categoría", description = "Actualiza los datos de una categoría existente.")
    public ResponseEntity<ApiResponseDTO<CategoriaInsumoResponse>> update(@PathVariable Integer id,
                                                                          @Valid @RequestBody CategoriaInsumoRequest request,
                                                                          HttpServletRequest servletRequest) {
        CategoriaInsumoResponse categoria = categoriaService.update(id, request);
        ApiResponseDTO<CategoriaInsumoResponse> body = ApiResponseDTO.success(HttpStatus.OK,
                "Categoría actualizada exitosamente", servletRequest.getRequestURI(), categoria);
        return ResponseEntity.ok(body);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar categoría", description = "Elimina una categoría del sistema.")
    public ResponseEntity<ApiResponseDTO<Void>> delete(@PathVariable Integer id, HttpServletRequest servletRequest) {
        categoriaService.delete(id);
        ApiResponseDTO<Void> body = ApiResponseDTO.success(HttpStatus.OK,
                "Categoría eliminada exitosamente", servletRequest.getRequestURI(), null);
        return ResponseEntity.ok(body);
    }

    @PutMapping("/{id}/deshabilitar")
    @Operation(summary = "Deshabilitar categoría",
            description = "Aplica baja lógica: la categoría queda inactiva, deja de listarse en el formulario "
                    + "y en el inventario, pero el registro se conserva en la base de datos.")
    public ResponseEntity<ApiResponseDTO<CategoriaInsumoResponse>> deshabilitar(@PathVariable Integer id,
                                                                                HttpServletRequest servletRequest) {
        CategoriaInsumoResponse categoria = categoriaService.deshabilitar(id);
        ApiResponseDTO<CategoriaInsumoResponse> body = ApiResponseDTO.success(HttpStatus.OK,
                "Categoría deshabilitada exitosamente", servletRequest.getRequestURI(), categoria);
        return ResponseEntity.ok(body);
    }

}