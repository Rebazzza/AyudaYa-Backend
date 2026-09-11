package com.donaciones.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Solicitud para cambiar el estado de una donación (genera una entrada en el historial)")
public class ActualizarEstadoRequest {

    @Schema(example = "EN_TRANSITO")
    @NotBlank(message = "El nuevo estado es obligatorio")
    @Size(max = 25, message = "El estado no debe superar los 25 caracteres")
    private String estado;

    @Schema(example = "Donación recogida por el transportista")
    @Size(max = 255, message = "La observación no debe superar los 255 caracteres")
    private String observacionHistorial;

    @Schema(description = "Opcional. Local relacionado con el cambio de estado")
    private Long idLocal;

    @Schema(description = "Opcional. Trabajador responsable del cambio de estado")
    private Integer idTrabajador;

}