package com.donaciones.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Información de una donación con sus detalles")
public class DonacionResponse {

    private Integer idDonacion;

    private String codigoSeguimiento;

    private LocalDateTime fechaRegistro;

    private LocalDateTime fechaExpiracion;

    private LocalDateTime fechaVerificacion;

    private String estadoActual;

    private Long idUsuario;

    private String dniDonante;

    private String nombreDonante;

    private Long idLocalRecepcion;

    private String nombreLocal;

    private Integer idTrabajador;

    private String nombreTrabajador;

    private List<DetalleDonacionResponse> detalles;

}