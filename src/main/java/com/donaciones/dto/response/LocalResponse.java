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
@Schema(description = "Información de un local de recepción")
public class LocalResponse {

    private Long idLocal;

    private String nombreLocal;

    private String direccionLocal;

    private BigDecimal latitud;

    private BigDecimal longitud;

    private Double capacidadLocalM3;

    private String telefonoLocal;

    private Boolean estadoActivo;

}