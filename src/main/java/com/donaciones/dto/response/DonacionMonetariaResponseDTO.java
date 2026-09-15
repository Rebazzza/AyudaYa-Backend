package com.donaciones.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Donación monetaria registrada")
public class DonacionMonetariaResponseDTO {

    private Integer idDonacionMonetaria;

    private Long idUsuario;

    private String nombreDonante;

    private BigDecimal monto;

    private String moneda;

    private String metodoPago;

    private String numeroOperacion;

    private String comprobanteUrl;

    private String estadoVerificacion;

    private LocalDateTime fechaRegistro;

}