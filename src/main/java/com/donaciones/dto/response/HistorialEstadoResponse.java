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
@Schema(description = "Entrada del historial de estados de una donación")
public class HistorialEstadoResponse {

    private Integer idHistorial;

    private Integer idDonacion;

    private String codigoSeguimiento;

    private String estado;

    private LocalDateTime fechaCambio;

    private String observacionHistorial;

    private Long idLocal;

    private String nombreLocal;

    private Integer idTrabajador;

    private String nombreTrabajador;

}