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
@Schema(description = "Información de un trabajador")
public class TrabajadorResponse {

    private Integer idTrabajador;

    private String cargoTrabajador;

    private LocalDateTime fechaContratacion;

    private Long idUsuario;

    private String dniUsuario;

    private String nombreCompletoUsuario;

    private Long idLocal;

    private String nombreLocal;

}