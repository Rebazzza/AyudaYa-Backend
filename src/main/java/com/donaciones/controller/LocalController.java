package com.donaciones.controller;

import com.donaciones.dto.ApiResponseDTO;
import com.donaciones.dto.request.LocalRequest;
import com.donaciones.dto.response.LocalResponse;
import com.donaciones.service.LocalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/locales")
@RequiredArgsConstructor
@Tag(name = "Locales de Recepción", description = "Gestión de los locales de recepción de donaciones")
public class LocalController {

    private final LocalService localService;

    @GetMapping
    @Operation(summary = "Listar locales activos",
            description = "Devuelve los locales con estadoActivo = true para que los donantes elijan a dónde ir.")
    public ResponseEntity<ApiResponseDTO<List<LocalResponse>>> listActive(HttpServletRequest servletRequest) {
        List<LocalResponse> locales = localService.listActive();
        ApiResponseDTO<List<LocalResponse>> body = ApiResponseDTO.success(HttpStatus.OK,
                "Locales obtenidos exitosamente", servletRequest.getRequestURI(), locales);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener local por ID", description = "Devuelve el detalle de un local de recepción.")
    public ResponseEntity<ApiResponseDTO<LocalResponse>> getById(@PathVariable Long id,
                                                                 HttpServletRequest servletRequest) {
        LocalResponse local = localService.getById(id);
        ApiResponseDTO<LocalResponse> body = ApiResponseDTO.success(HttpStatus.OK,
                "Local obtenido exitosamente", servletRequest.getRequestURI(), local);
        return ResponseEntity.ok(body);
    }

    @PostMapping
    @Operation(summary = "Registrar local de recepción",
            description = "Registra un nuevo local con capacidad en m3 mayor a 0. Se crea con estadoActivo = true.")
    public ResponseEntity<ApiResponseDTO<LocalResponse>> create(@Valid @RequestBody LocalRequest request,
                                                                HttpServletRequest servletRequest) {
        LocalResponse local = localService.create(request);
        ApiResponseDTO<LocalResponse> body = ApiResponseDTO.success(HttpStatus.CREATED,
                "Local registrado exitosamente", servletRequest.getRequestURI(), local);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar local de recepción",
            description = "Actualiza los datos de un local existente, incluida su ubicación (latitud y longitud).")
    public ResponseEntity<ApiResponseDTO<LocalResponse>> update(@PathVariable Long id,
                                                                @Valid @RequestBody LocalRequest request,
                                                                HttpServletRequest servletRequest) {
        LocalResponse local = localService.update(id, request);
        ApiResponseDTO<LocalResponse> body = ApiResponseDTO.success(HttpStatus.OK,
                "Local actualizado exitosamente", servletRequest.getRequestURI(), local);
        return ResponseEntity.ok(body);
    }

}