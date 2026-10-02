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
@Schema(description = "Producto almacenado en un centro de acopio, con su estado de conservación/vencimiento")
public class ProductoInventarioDTO {

    private Integer idDetalle;

    private Integer idDonacion;

    private String codigoSeguimiento;

    private Integer idCategoria;

    private String nombreCategoria;

    private String descripcionDetalle;

    private BigDecimal cantidad;

    private String unidadMedida;

    private Boolean requiereRefrigeracion;

    private LocalDateTime fechaVencimiento;

    @Schema(description = "VIGENTE, POR_VENCER (menos de 15 días), VENCIDO o SIN_VENCIMIENTO")
    private String estadoConservacion;

}
