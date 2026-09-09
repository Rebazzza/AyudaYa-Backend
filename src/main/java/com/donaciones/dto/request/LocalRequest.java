package com.donaciones.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Solicitud de registro de un local de recepción")
public class LocalRequest {

    @Schema(example = "Coliseo Manuel Bonilla")
    @NotBlank(message = "El nombre del local es obligatorio")
    @Size(max = 100, message = "El nombre del local no debe superar los 100 caracteres")
    private String nombreLocal;

    @Schema(example = "Av. Prolongación Iquitos s/n, Miraflores, Lima")
    @NotBlank(message = "La dirección del local es obligatoria")
    @Size(max = 255, message = "La dirección no debe superar los 255 caracteres")
    private String direccionLocal;

    @Schema(example = "-12.046374")
    @NotNull(message = "La latitud es obligatoria")
    @DecimalMin(value = "-90.0", message = "La latitud debe estar entre -90 y 90")
    @DecimalMax(value = "90.0", message = "La latitud debe estar entre -90 y 90")
    private BigDecimal latitud;

    @Schema(example = "-77.042793")
    @NotNull(message = "La longitud es obligatoria")
    @DecimalMin(value = "-180.0", message = "La longitud debe estar entre -180 y 180")
    @DecimalMax(value = "180.0", message = "La longitud debe estar entre -180 y 180")
    private BigDecimal longitud;

    @Schema(example = "1200.5")
    @NotNull(message = "La capacidad en m3 es obligatoria")
    @DecimalMin(value = "0.0", inclusive = false, message = "La capacidad en m3 debe ser mayor a 0")
    private Double capacidadLocalM3;

    @Schema(example = "4455667")
    @NotBlank(message = "El teléfono del local es obligatorio")
    @Size(max = 20, message = "El teléfono no debe superar los 20 caracteres")
    private String telefonoLocal;

}