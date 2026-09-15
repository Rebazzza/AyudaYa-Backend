package com.donaciones.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotBlank;
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
@Schema(description = "Solicitud para armar N kits a partir del stock verificado de un local")
public class CrearKitRequestDTO {

    @Schema(example = "1")
    @NotNull(message = "El id del local es obligatorio")
    private Long idLocal;

    @Schema(example = "Kit Familiar de Alimentos")
    @NotBlank(message = "El nombre del kit es obligatorio")
    private String nombreKit;

    @Schema(example = "10", description = "Cantidad de kits a formar")
    @NotNull(message = "La cantidad de kits es obligatoria")
    @Min(value = 1, message = "La cantidad de kits debe ser al menos 1")
    private Integer cantidadKitsAFormar;

    @Schema(description = "Insumos (por categoría) que compone cada kit")
    @Valid
    @NotEmpty(message = "La lista de insumos del kit no puede estar vacía")
    private List<InsumoKitRequestDTO> insumosPorKit;

}