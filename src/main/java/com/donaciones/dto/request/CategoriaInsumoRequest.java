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
@Schema(description = "Solicitud de registro o actualización de una categoría de insumo")
public class CategoriaInsumoRequest {

    @Schema(example = "Alimentos No Perecibles")
    @NotBlank(message = "El nombre de la categoría es obligatorio")
    @Size(max = 50, message = "El nombre no debe superar los 50 caracteres")
    private String nombreCategoria;

    @Schema(example = "kg")
    @NotBlank(message = "La unidad de medida es obligatoria")
    @Size(max = 20, message = "La unidad de medida no debe superar los 20 caracteres")
    private String unidadMedCate;

    @Schema(example = "false", description = "Indica si el producto requiere refrigeración. Por defecto false.")
    private Boolean refrigerar;

}