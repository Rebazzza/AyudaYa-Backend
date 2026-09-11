package com.donaciones.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Alerta de insumos próximos a vencer (menos de 15 días)")
public class AlertaCaducidadDTO {

    private Integer idDonacion;

    private String codigoSeguimiento;

    private String nombreCategoria;

    private BigDecimal cantidad;

    private LocalDateTime fechaVencimiento;

    private Long diasParaCaducar;

}