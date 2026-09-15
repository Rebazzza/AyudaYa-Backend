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
@Schema(description = "Insumo contenido en un kit")
public class KitDetalleResponseDTO {

    private Integer idDetalleKit;

    private Integer idCategoria;

    private String nombreCategoria;

    private String unidadMedida;

    private BigDecimal cantidadInsumo;

}