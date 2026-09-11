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
@Schema(description = "Posición geográfica actual de una donación para el mapa del donante")
public class UbicacionActualResponseDTO {

    private String codigoSeguimiento;

    private String estadoActual;

    private String nombreLocal;

    private String direccionLocal;

    private Double latitudGPS;

    private Double longitudGPS;

    private LocalDateTime fechaUltimoEscaneo;

}