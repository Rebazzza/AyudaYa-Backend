package com.donaciones.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Solicitud de registro de una notificación")
public class NotificacionRequest {

    @Schema(example = "1")
    @NotNull(message = "El id del usuario destinatario es obligatorio")
    private Long idUsuario;

    @Schema(description = "Opcional. Donación relacionada con la notificación")
    private Integer idDonacion;

    @Schema(example = "Tu donación fue recibida en el local")
    @NotBlank(message = "El mensaje es obligatorio")
    @Size(max = 255, message = "El mensaje no debe superar los 255 caracteres")
    private String mensajeNoti;

}