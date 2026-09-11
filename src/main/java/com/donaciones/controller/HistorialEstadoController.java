package com.donaciones.controller;

import com.donaciones.dto.ApiResponseDTO;
import com.donaciones.dto.request.HistorialEstadoRequest;
import com.donaciones.dto.response.HistorialEstadoResponse;
import com.donaciones.service.HistorialEstadoService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/historial")
@RequiredArgsConstructor
@Tag(name = "Historial de Estados", description = "Auditoría de cambios de estado de las donaciones")
public class HistorialEstadoController {

    private final HistorialEstadoService historialService;

    @GetMapping
    @Operation(summary = "Listar historial de estados", description = "Devuelve todo el historial (más reciente primero).")
    public ResponseEntity<ApiResponseDTO<List<HistorialEstadoResponse>>> list(HttpServletRequest servletRequest) {
        List<HistorialEstadoResponse> historial = historialService.list();
        ApiResponseDTO<List<HistorialEstadoResponse>> body = ApiResponseDTO.success(HttpStatus.OK,
                "Historial obtenido exitosamente", servletRequest.getRequestURI(), historial);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener registro de historial por ID", description = "Devuelve una entrada del historial.")
    public ResponseEntity<ApiResponseDTO<HistorialEstadoResponse>> getById(@PathVariable Integer id,
                                                                           HttpServletRequest servletRequest) {
        HistorialEstadoResponse historial = historialService.getById(id);
        ApiResponseDTO<HistorialEstadoResponse> body = ApiResponseDTO.success(HttpStatus.OK,
                "Registro obtenido exitosamente", servletRequest.getRequestURI(), historial);
        return ResponseEntity.ok(body);
    }

    @PostMapping
    @Operation(summary = "Registrar entrada de historial", description = "Registra manualmente un cambio de estado para una donación.")
    public ResponseEntity<ApiResponseDTO<HistorialEstadoResponse>> create(@Valid @RequestBody HistorialEstadoRequest request,
                                                                          HttpServletRequest servletRequest) {
        HistorialEstadoResponse historial = historialService.create(request);
        ApiResponseDTO<HistorialEstadoResponse> body = ApiResponseDTO.success(HttpStatus.CREATED,
                "Cambio de estado registrado", servletRequest.getRequestURI(), historial);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar registro de historial", description = "Elimina una entrada del historial.")
    public ResponseEntity<ApiResponseDTO<Void>> delete(@PathVariable Integer id, HttpServletRequest servletRequest) {
        historialService.delete(id);
        ApiResponseDTO<Void> body = ApiResponseDTO.success(HttpStatus.OK,
                "Registro eliminado exitosamente", servletRequest.getRequestURI(), null);
        return ResponseEntity.ok(body);
    }

}