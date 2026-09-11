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
@Schema(description = "Información completa de una donación registrada (con código generado e ítems)")
public class DonacionResponseDTO {

    private Integer idDonacion;

    private String codigoSeguimiento;

    private LocalDateTime fechaRegistro;

    private String estadoActual;

    private Long idUsuario;

    private String nombreDonante;

    private Long idLocalRecepcion;

    private String nombreLocal;

    private String direccionLocal;

    private List<DetalleDonacionResponse> detalles;

}