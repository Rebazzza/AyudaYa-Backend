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
@Schema(description = "Detalle de un producto dentro de una donación")
public class DetalleDonacionResponse {

    private Integer idDetalle;

    private Integer idCategoria;

    private String nombreCategoria;

    private String descripcionDetalle;

    private BigDecimal cantidadDeclarada;

    private BigDecimal cantidadVerificada;

    private LocalDateTime fechaVencimiento;

    private String observacionDetalle;

    private Boolean activo;

}