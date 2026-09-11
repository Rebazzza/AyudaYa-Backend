package com.donaciones.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Solicitud de corroboración de recepción de una donación en el almacén")
public class CorroboracionRequestDTO {

    @Schema(example = "5")
    @NotNull(message = "El id de la donación es obligatorio")
    private Integer idDonacion;

    @Schema(example = "1")
    @NotNull(message = "El id del trabajador es obligatorio")
    private Integer idTrabajador;

    @Schema(example = "1", description = "Centro de acopio donde se recibe la donación")
    @NotNull(message = "El id del local es obligatorio")
    private Long idLocal;

    @Schema(description = "Opcional. Latitud GPS capturada al momento de inspeccionar")
    @DecimalMin(value = "-90.0", message = "La latitud debe estar entre -90 y 90")
    @DecimalMax(value = "90.0", message = "La latitud debe estar entre -90 y 90")
    private Double latitud;

    @Schema(description = "Opcional. Longitud GPS capturada al momento de inspeccionar")
    @DecimalMin(value = "-180.0", message = "La longitud debe estar entre -180 y 180")
    @DecimalMax(value = "180.0", message = "La longitud debe estar entre -180 y 180")
    private Double longitud;

    @Schema(example = "Lote con empaque dañado en un costal", description = "Opcional. Observaciones generales de la inspección")
    @Size(max = 255, message = "Las observaciones no deben superar los 255 caracteres")
    private String observaciones;

    @Schema(description = "Detalles con las cantidades verificadas")
    @Valid
    @NotEmpty(message = "Debe verificar al menos un insumo")
    private List<ItemCorroboracionRequestDTO> detalles;

}