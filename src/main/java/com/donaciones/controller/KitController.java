package com.donaciones.controller;

import com.donaciones.dto.ApiResponseDTO;
import com.donaciones.dto.request.CrearKitRequestDTO;
import com.donaciones.dto.response.KitResponseDTO;
import com.donaciones.service.KitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

import java.util.List;

@RestController
@RequestMapping("/api/v1/kits")
@RequiredArgsConstructor
@Tag(name = "Kits de Ayuda", description = "Armado, clasificación y consulta de kits")
public class KitController {

    private final KitService kitService;

    @Operation(summary = "Armar N kits descontando el stock verificado del local")
    @PostMapping("/armar")
    public ResponseEntity<ApiResponseDTO<List<KitResponseDTO>>> armar(
            @Valid @RequestBody CrearKitRequestDTO dto) {
        List<KitResponseDTO> respuesta = kitService.armarKits(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success(HttpStatus.CREATED,
                        "Kits armados correctamente", "/api/v1/kits/armar", respuesta));
    }

    @Operation(summary = "Listar los kits del local de recepción")
    @GetMapping("/local/{idLocal}")
    public ResponseEntity<ApiResponseDTO<List<KitResponseDTO>>> listarPorLocal(
            @PathVariable Long idLocal) {
        List<KitResponseDTO> respuesta = kitService.listarKitsPorLocal(idLocal);
        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.OK,
                "Kits del local", "/api/v1/kits/local/" + idLocal, respuesta));
    }

    @Operation(summary = "Obtener un kit por su código de seguimiento")
    @GetMapping("/{codigoKit}")
    public ResponseEntity<ApiResponseDTO<KitResponseDTO>> obtener(
            @PathVariable String codigoKit) {
        KitResponseDTO respuesta = kitService.obtenerKitPorCodigo(codigoKit);
        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.OK,
                "Kit encontrado", "/api/v1/kits/" + codigoKit, respuesta));
    }

}