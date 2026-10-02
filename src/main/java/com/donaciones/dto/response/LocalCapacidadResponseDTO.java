package com.donaciones.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Capacidad declarada de un local de recepción frente a su ocupación estimada. "
        + "La ocupación se estima contando las donaciones en estado EN_ALMACEN registradas en ese local, "
        + "ya que el esquema actual no registra un volumen en m3 por donación o por categoría.")
public class LocalCapacidadResponseDTO {

    private Long idLocal;

    private String nombreLocal;

    private Double capacidadLocalM3;

    private Long donacionesEnAlmacen;

    private Boolean lleno;

}
