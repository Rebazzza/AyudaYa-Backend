package com.donaciones.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Solicitud de registro de una nueva donación (el código de seguimiento se genera automáticamente)")
public class DonacionRegistroRequestDTO {

    @Schema(example = "1")
    @NotNull(message = "El id del usuario donante es obligatorio")
    private Long idUsuario;

    @Schema(example = "1")
    @NotNull(message = "El id del local de recepción es obligatorio")
    private Long idLocalRecepcion;

    @Schema(description = "Detalle de los productos donados")
    @Valid
    @NotEmpty(message = "La donación debe contener al menos un detalle")
    private List<DetalleDonacionRequestDTO> detalles;

}