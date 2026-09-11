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
@Schema(description = "Información de una notificación")
public class NotificacionResponse {

    private Integer idNotificacion;

    private Long idUsuario;

    private Integer idDonacion;

    private String mensajeNoti;

    private LocalDateTime fechaEnvio;

    private Boolean leido;

}