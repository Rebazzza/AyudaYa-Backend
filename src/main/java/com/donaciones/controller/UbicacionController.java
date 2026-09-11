package com.donaciones.controller;

import com.donaciones.dto.ApiResponseDTO;
import com.donaciones.dto.request.ActualizarUbicacionRequestDTO;
import com.donaciones.dto.response.UbicacionActualResponseDTO;
import com.donaciones.service.DonacionService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ubicaciones")
@RequiredArgsConstructor
@Tag(name = "Ubicaciones", description = "Registro y consulta de coordenadas GPS de las donaciones")
public class UbicacionController {

    private final DonacionService donacionService;

    @PostMapping("/escaneo")
    @Operation(summary = "Registrar escaneo de ubicación",
            description = "Cambia el estado de la donación y registra las coordenadas GPS capturadas por el trabajador en el punto de recepción.")
    public ResponseEntity<ApiResponseDTO<UbicacionActualResponseDTO>> escaneo(
            @Valid @RequestBody ActualizarUbicacionRequestDTO request,
            HttpServletRequest servletRequest) {
        UbicacionActualResponseDTO ubicacion = donacionService.actualizarUbicacionYEstado(request);
        ApiResponseDTO<UbicacionActualResponseDTO> body = ApiResponseDTO.success(HttpStatus.OK,
                "Ubicación y estado actualizados exitosamente", servletRequest.getRequestURI(), ubicacion);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/tracking/{codigoSeguimiento}")
    @Operation(summary = "Obtener ubicación actual",
            description = "Devuelve la posición geográfica y el centro de acopio actual de la donación para el mapa del donante.")
    public ResponseEntity<ApiResponseDTO<UbicacionActualResponseDTO>> tracking(@PathVariable String codigoSeguimiento,
                                                                               HttpServletRequest servletRequest) {
        UbicacionActualResponseDTO ubicacion = donacionService.obtenerUbicacionActual(codigoSeguimiento);
        ApiResponseDTO<UbicacionActualResponseDTO> body = ApiResponseDTO.success(HttpStatus.OK,
                "Ubicación actual obtenida exitosamente", servletRequest.getRequestURI(), ubicacion);
        return ResponseEntity.ok(body);
    }

}