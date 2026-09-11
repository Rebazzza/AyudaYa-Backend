package com.donaciones.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Entrada del historial dentro de la línea de tiempo de seguimiento de una donación")
public class TrackingHistorialDTO {

    private String estado;

    private LocalDateTime fechaCambio;

    private String observacion;

    private String nombreLocal;

}