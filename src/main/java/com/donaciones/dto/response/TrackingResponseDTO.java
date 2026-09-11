package com.donaciones.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Línea de tiempo de seguimiento de una donación consultada por su código")
public class TrackingResponseDTO {

    private String codigoSeguimiento;

    private String estadoActual;

    private String nombreLocal;

    private String direccionLocal;

    private LocalDateTime fechaRegistro;

    private List<TrackingHistorialDTO> historial;

}