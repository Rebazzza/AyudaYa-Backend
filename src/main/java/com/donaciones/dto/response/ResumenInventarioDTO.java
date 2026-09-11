package com.donaciones.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resumen de stock verificado por categoría en un centro de acopio")
public class ResumenInventarioDTO {

    private Integer idCategoria;

    private String nombreCategoria;

    private String unidadMedida;

    private BigDecimal stockTotalVerificado;

    private Long totalItemsIncidencia;

    private Boolean requiereRefrigeracion;

}