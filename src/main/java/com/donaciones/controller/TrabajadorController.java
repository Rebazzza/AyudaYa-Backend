package com.donaciones.controller;

import com.donaciones.dto.ApiResponseDTO;
import com.donaciones.dto.request.TrabajadorRequest;
import com.donaciones.dto.response.TrabajadorResponse;
import com.donaciones.service.TrabajadorService;
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
@RequestMapping("/api/v1/trabajadores")
@RequiredArgsConstructor
@Tag(name = "Trabajadores", description = "Gestión de trabajadores de los centros de acopio")
public class TrabajadorController {

    private final TrabajadorService trabajadorService;

    @GetMapping
    @Operation(summary = "Listar trabajadores", description = "Devuelve todos los trabajadores registrados.")
    public ResponseEntity<ApiResponseDTO<List<TrabajadorResponse>>> list(HttpServletRequest servletRequest) {
        List<TrabajadorResponse> trabajadores = trabajadorService.list();
        ApiResponseDTO<List<TrabajadorResponse>> body = ApiResponseDTO.success(HttpStatus.OK,
                "Trabajadores obtenidos exitosamente", servletRequest.getRequestURI(), trabajadores);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener trabajador por ID", description = "Devuelve el detalle de un trabajador.")
    public ResponseEntity<ApiResponseDTO<TrabajadorResponse>> getById(@PathVariable Integer id,
                                                                      HttpServletRequest servletRequest) {
        TrabajadorResponse trabajador = trabajadorService.getById(id);
        ApiResponseDTO<TrabajadorResponse> body = ApiResponseDTO.success(HttpStatus.OK,
                "Trabajador obtenido exitosamente", servletRequest.getRequestURI(), trabajador);
        return ResponseEntity.ok(body);
    }

    @PostMapping
    @Operation(summary = "Registrar trabajador",
            description = "Registra un usuario como trabajador, vinculándolo a un local de recepción.")
    public ResponseEntity<ApiResponseDTO<TrabajadorResponse>> create(@Valid @RequestBody TrabajadorRequest request,
                                                                     HttpServletRequest servletRequest) {
        TrabajadorResponse trabajador = trabajadorService.create(request);
        ApiResponseDTO<TrabajadorResponse> body = ApiResponseDTO.success(HttpStatus.CREATED,
                "Trabajador registrado exitosamente", servletRequest.getRequestURI(), trabajador);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar trabajador", description = "Actualiza los datos de un trabajador existente.")
    public ResponseEntity<ApiResponseDTO<TrabajadorResponse>> update(@PathVariable Integer id,
                                                                     @Valid @RequestBody TrabajadorRequest request,
                                                                     HttpServletRequest servletRequest) {
        TrabajadorResponse trabajador = trabajadorService.update(id, request);
        ApiResponseDTO<TrabajadorResponse> body = ApiResponseDTO.success(HttpStatus.OK,
                "Trabajador actualizado exitosamente", servletRequest.getRequestURI(), trabajador);
        return ResponseEntity.ok(body);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar trabajador", description = "Elimina un trabajador del sistema.")
    public ResponseEntity<ApiResponseDTO<Void>> delete(@PathVariable Integer id, HttpServletRequest servletRequest) {
        trabajadorService.delete(id);
        ApiResponseDTO<Void> body = ApiResponseDTO.success(HttpStatus.OK,
                "Trabajador eliminado exitosamente", servletRequest.getRequestURI(), null);
        return ResponseEntity.ok(body);
    }

}