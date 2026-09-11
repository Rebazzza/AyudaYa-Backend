package com.donaciones.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Solicitud de registro o actualización de un trabajador")
public class TrabajadorRequest {

    @Schema(example = "1")
    @NotNull(message = "El id del usuario es obligatorio")
    private Long idUsuario;

    @Schema(example = "1")
    @NotNull(message = "El id del local es obligatorio")
    private Long idLocal;

    @Schema(example = "Coordinador de Acopio")
    @NotBlank(message = "El cargo es obligatorio")
    @Size(max = 50, message = "El cargo no debe superar los 50 caracteres")
    private String cargoTrabajador;

    @Schema(description = "Opcional. Fecha de contratación. Si se omite, se usa la fecha actual.")
    private LocalDateTime fechaContratacion;

}