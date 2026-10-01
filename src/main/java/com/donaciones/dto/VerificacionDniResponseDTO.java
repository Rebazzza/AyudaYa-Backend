package com.donaciones.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resultado de la verificación biométrica de DNI de un donante. "
        + "No expone rutas de archivos ni datos biométricos intermedios (RF18)")
public class VerificacionDniResponseDTO {

    @Schema(description = "Identificador del usuario donante verificado", example = "12")
    private Integer idUsuario;

    @Schema(description = "Resultado final de la verificación de identidad", example = "true")
    private Boolean dniVerificado;

    @Schema(description = "Mensaje final del proceso de verificación", example = "DNI verificado exitosamente")
    private String mensajeResultado;

}
