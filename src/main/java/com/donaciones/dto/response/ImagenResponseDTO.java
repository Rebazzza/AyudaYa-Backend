package com.donaciones.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Información de una imagen cargada al sistema")
public class ImagenResponseDTO {

    private Integer idImagen;

    private String nombreOriginal;

    private String rutaUrl;

    private String tipoImagen;

    private LocalDateTime fechaCarga;

}