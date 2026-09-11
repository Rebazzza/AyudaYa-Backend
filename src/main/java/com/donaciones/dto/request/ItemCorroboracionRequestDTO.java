package com.donaciones.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Corroboración de un insumo específico de la donación")
public class ItemCorroboracionRequestDTO {

    @Schema(example = "3", description = "Id del detalle de la donación a corroborar")
    @NotNull(message = "El id del detalle es obligatorio")
    private Integer idDetalle;

    @Schema(example = "9.50", description = "Cantidad realmente recibida y verificada")
    @NotNull(message = "La cantidad verificada es obligatoria")
    @DecimalMin(value = "0.0", message = "La cantidad verificada no puede ser negativa")
    private BigDecimal cantidadVerificada;

    @Schema(description = "Opcional. Fecha de vencimiento para insumos perecibles")
    private LocalDateTime fechaVencimiento;

}