package com.donaciones.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
@Schema(description = "Detalle de un producto dentro del registro de una donación")
public class DetalleDonacionRequestDTO {

    @Schema(example = "1")
    @NotNull(message = "El id de la categoría es obligatorio")
    private Integer idCategoria;

    @Schema(example = "Arroz Costeño 5kg")
    @Size(max = 200, message = "La descripción no debe superar los 200 caracteres")
    private String descripcionDetalle;

    @Schema(example = "10.00")
    @NotNull(message = "La cantidad declarada es obligatoria")
    @DecimalMin(value = "0.0", inclusive = false, message = "La cantidad declarada debe ser mayor a 0")
    private BigDecimal cantidadDeclarada;

    @Schema(description = "Opcional. Fecha de vencimiento del producto")
    private LocalDateTime fechaVencimiento;

}