package com.donaciones.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Solicitud de registro o actualización de una donación (cabecera con sus detalles)")
public class DonacionRequest {

    @Schema(example = "DON-2026-0001")
    @NotBlank(message = "El código de seguimiento es obligatorio")
    @Size(max = 15, message = "El código de seguimiento no debe superar los 15 caracteres")
    private String codigoSeguimiento;

    @Schema(description = "Opcional. Fecha de expiración de la donación")
    private LocalDateTime fechaExpiracion;

    @Schema(description = "Opcional. Fecha de verificación de la donación")
    private LocalDateTime fechaVerificacion;

    @Schema(example = "CREADA")
    @NotBlank(message = "El estado actual es obligatorio")
    @Size(max = 25, message = "El estado actual no debe superar los 25 caracteres")
    private String estadoActual;

    @Schema(example = "1")
    @NotNull(message = "El id del usuario donante es obligatorio")
    private Long idUsuario;

    @Schema(example = "1")
    @NotNull(message = "El id del local de recepción es obligatorio")
    private Long idLocalRecepcion;

    @Schema(description = "Opcional. Trabajador que registra la donación")
    private Integer idTrabajador;

    @Schema(description = "Detalle de los productos donados")
    @Valid
    @NotEmpty(message = "La donación debe contener al menos un detalle")
    private List<DetalleDonacionRequest> detalles;

}