package com.donaciones.controller;

import com.donaciones.dto.ApiResponseDTO;
import com.donaciones.dto.request.RegistroMonetarioRequestDTO;
import com.donaciones.dto.request.VerificarFondosRequestDTO;
import com.donaciones.dto.response.DonacionMonetariaResponseDTO;
import com.donaciones.dto.response.TotalFondosDTO;
import com.donaciones.service.DonacionMonetariaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@RestController
@RequestMapping("/api/v1/donaciones-monetarias")
@RequiredArgsConstructor
@Tag(name = "Donaciones Monetarias", description = "Registro, verificación y consulta de fondos recaudados")
public class DonacionMonetariaController {

    private final DonacionMonetariaService donacionMonetariaService;

    @Operation(summary = "Registrar una donación monetaria (queda en estado PENDIENTE)")
    @PostMapping
    public ResponseEntity<ApiResponseDTO<DonacionMonetariaResponseDTO>> registrar(
            @Valid @RequestBody RegistroMonetarioRequestDTO dto) {
        DonacionMonetariaResponseDTO respuesta = donacionMonetariaService.registrarDonacionMonetaria(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success(HttpStatus.CREATED,
                        "Donación monetaria registrada correctamente",
                        "/api/v1/donaciones-monetarias", respuesta));
    }

    @Operation(summary = "Verificar o rechazar los fondos de una donación monetaria")
    @PutMapping("/{id}/verificar")
    public ResponseEntity<ApiResponseDTO<DonacionMonetariaResponseDTO>> verificar(
            @PathVariable Integer id,
            @Valid @RequestBody VerificarFondosRequestDTO dto) {
        DonacionMonetariaResponseDTO respuesta = donacionMonetariaService.verificarFondos(id, dto.getEstado());
        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.OK,
                "Donación monetaria actualizada a " + respuesta.getEstadoVerificacion(),
                "/api/v1/donaciones-monetarias/" + id + "/verificar", respuesta));
    }

    @Operation(summary = "Obtener el total recaudado en donaciones monetarias verificadas")
    @GetMapping("/total")
    public ResponseEntity<ApiResponseDTO<TotalFondosDTO>> total() {
        TotalFondosDTO respuesta = donacionMonetariaService.obtenerTotalFondosRecaudados();
        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.OK,
                "Total de fondos recaudados", "/api/v1/donaciones-monetarias/total", respuesta));
    }

}