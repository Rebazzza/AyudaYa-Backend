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
@Schema(description = "Información del usuario autenticado o registrado")
public class UsuarioResponse {

    private Long idUsuario;

    private String dniUsuario;

    private String nombreUsuario;

    private String apellidosUsuario;

    private String correoUsuario;

    private String telefonoUsuario;

    private String nombreRol;

    private LocalDateTime fechaRegistro;

}