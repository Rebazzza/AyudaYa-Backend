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
@Schema(description = "Dictamen devuelto por el microservicio de Inteligencia Artificial (Flask) al verificar la foto de un DNI")
public class RespuestaIaDniDTO {

    @Schema(description = "Indica si el modelo de IA confirmó que la imagen corresponde a un DNI legible",
            example = "true")
    private Boolean esValido;

    @Schema(description = "Nivel de confianza del modelo sobre el dictamen, entre 0.0 y 1.0",
            example = "0.95")
    private Double confianza;

    @Schema(description = "Descripción legible del resultado del análisis", example = "DNI validado correctamente")
    private String mensaje;

}
