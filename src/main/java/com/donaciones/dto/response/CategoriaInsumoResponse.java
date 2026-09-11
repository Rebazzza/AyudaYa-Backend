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
@Schema(description = "Información de una categoría de insumo")
public class CategoriaInsumoResponse {

    private Integer idCategoria;

    private String nombreCategoria;

    private String unidadMedCate;

    private Boolean refrigerar;

}