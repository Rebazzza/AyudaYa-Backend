package com.donaciones.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Insumo requerido para armar un kit")
public class InsumoKitRequestDTO {

    @Schema(example = "1")
    @NotNull(message = "El id de categoría es obligatorio")
    private Integer idCategoria;

    @Schema(example = "3.00", description = "Cantidad del insumo por cada kit")
    @NotNull(message = "La cantidad por kit es obligatoria")
    @DecimalMin(value = "0.01", message = "La cantidad por kit debe ser mayor a 0")
    private BigDecimal cantidad;

}