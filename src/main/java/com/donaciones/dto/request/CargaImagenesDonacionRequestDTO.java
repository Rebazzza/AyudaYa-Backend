package com.donaciones.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Metadatos de una carga de imágenes asociadas a una donación (se envían como campos de formulario junto a los archivos)")
public class CargaImagenesDonacionRequestDTO {

    @Schema(example = "5", description = "Id de la donación a la que pertenecen las imágenes")
    @NotNull(message = "El id de la donación es obligatorio")
    private Integer idDonacion;

    @Schema(example = "EVIDENCIA_RECEPCION", description = "Tipo de imagen (DNI_FRONTAL, DNI_POSTERIOR, EVIDENCIA_RECEPCION, EVIDENCIA_ENTREGA)")
    @NotBlank(message = "El tipo de imagen es obligatorio")
    private String tipoImagen;

}