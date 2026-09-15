package com.donaciones.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Cambio de estado de verificación de una donación monetaria")
public class VerificarFondosRequestDTO {

    @Schema(example = "VERIFICADO", allowableValues = {"VERIFICADO", "RECHAZADO"})
    @NotBlank(message = "El estado de verificación es obligatorio (VERIFICADO o RECHAZADO)")
    private String estado;

}