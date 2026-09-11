package com.donaciones.controller;

import com.donaciones.dto.ApiResponseDTO;
import com.donaciones.dto.request.CorroboracionRequestDTO;
import com.donaciones.dto.response.AlertaCaducidadDTO;
import com.donaciones.dto.response.DonacionResponse;
import com.donaciones.dto.response.ResumenInventarioDTO;
import com.donaciones.service.AlmacenService;
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

import java.util.List;

@RestController
@RequestMapping("/api/v1/almacen")
@RequiredArgsConstructor
@Tag(name = "Almacén", description = "Inspección de recepción, inventario y alertas de caducidad")
public class AlmacenController {

    private final AlmacenService almacenService;

    @PostMapping("/corroborar")
    @Operation(summary = "Corroborar recepción",
            description = "Compara lo declarado contra lo recibido, registra incidencias, GPS y pasa la donación a EN_ALMACEN. "
                    + "Genera notificación a administradores si más del 50% de ítems tienen incidencia.")
    public ResponseEntity<ApiResponseDTO<DonacionResponse>> corroborar(@Valid @RequestBody CorroboracionRequestDTO request,
                                                                        HttpServletRequest servletRequest) {
        DonacionResponse donacion = almacenService.corroborarRecepcion(request);
        ApiResponseDTO<DonacionResponse> body = ApiResponseDTO.success(HttpStatus.OK,
                "Corroboración registrada exitosamente", servletRequest.getRequestURI(), donacion);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/inventario/{idLocal}")
    @Operation(summary = "Inventario del local",
            description = "Resumen de stock verificado por categoría en un centro de acopio (donaciones en estado EN_ALMACEN).")
    public ResponseEntity<ApiResponseDTO<List<ResumenInventarioDTO>>> inventario(@PathVariable Long idLocal,
                                                                                 HttpServletRequest servletRequest) {
        List<ResumenInventarioDTO> inventario = almacenService.obtenerInventarioPorLocal(idLocal);
        ApiResponseDTO<List<ResumenInventarioDTO>> body = ApiResponseDTO.success(HttpStatus.OK,
                "Inventario obtenido exitosamente", servletRequest.getRequestURI(), inventario);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/alertas/caducidad/{idLocal}")
    @Operation(summary = "Alertas de caducidad",
            description = "Insumos almacenados en el local con fecha de vencimiento a menos de 15 días.")
    public ResponseEntity<ApiResponseDTO<List<AlertaCaducidadDTO>>> alertasCaducidad(@PathVariable Long idLocal,
                                                                                     HttpServletRequest servletRequest) {
        List<AlertaCaducidadDTO> alertas = almacenService.obtenerAlertasCaducidad(idLocal);
        ApiResponseDTO<List<AlertaCaducidadDTO>> body = ApiResponseDTO.success(HttpStatus.OK,
                "Alertas de caducidad obtenidas exitosamente", servletRequest.getRequestURI(), alertas);
        return ResponseEntity.ok(body);
    }

}