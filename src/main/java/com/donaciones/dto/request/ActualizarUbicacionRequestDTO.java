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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Solicitud de escaneo: registra el cambio de estado y las coordenadas GPS del punto de recepción")
public class ActualizarUbicacionRequestDTO {

    @Schema(example = "DON-2026-8X9FA1")
    @NotBlank(message = "El código de seguimiento es obligatorio")
    @Size(max = 15, message = "El código de seguimiento no debe superar los 15 caracteres")
    private String codigoSeguimiento;

    @Schema(example = "1", description = "Centro de acopio donde se realizó el escaneo")
    @NotNull(message = "El id del local de recepción es obligatorio")
    private Long idLocalRecepcion;

    @Schema(example = "1", description = "Opcional. Trabajador que realiza la operación")
    private Integer idTrabajador;

    @Schema(example = "EN_ALMACEN", description = "Estado al que cambia (EN_ALMACEN, EN_TRANSITO, ENTREGADO, etc.)")
    @NotBlank(message = "El nuevo estado es obligatorio")
    @Size(max = 25, message = "El nuevo estado no debe superar los 25 caracteres")
    private String nuevoEstado;

    @Schema(example = "-12.046374", description = "Opcional. Latitud GPS (-90 a 90). Si se omite, no se registra.")
    @DecimalMin(value = "-90.0", message = "La latitud debe estar entre -90 y 90")
    @DecimalMax(value = "90.0", message = "La latitud debe estar entre -90 y 90")
    private Double latitud;

    @Schema(example = "-77.042793", description = "Opcional. Longitud GPS (-180 a 180). Si se omite, no se registra.")
    @DecimalMin(value = "-180.0", message = "La longitud debe estar entre -180 y 180")
    @DecimalMax(value = "180.0", message = "La longitud debe estar entre -180 y 180")
    private Double longitud;

    @Schema(example = "Donación verificada e ingresada al almacén central")
    @Size(max = 255, message = "La observación no debe superar los 255 caracteres")
    private String observacion;

}