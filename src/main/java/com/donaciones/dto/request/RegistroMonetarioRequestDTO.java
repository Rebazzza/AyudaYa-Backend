package com.donaciones.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Registro de una donación monetaria (YAPE/PLIN/transferencia)")
public class RegistroMonetarioRequestDTO {

    @Schema(example = "1")
    @NotNull(message = "El id del usuario donante es obligatorio")
    private Long idUsuario;

    @Schema(example = "50.00")
    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
    private BigDecimal monto;

    @Schema(example = "PEN")
    private String moneda;

    @Schema(example = "YAPE")
    @NotBlank(message = "El método de pago es obligatorio")
    private String metodoPago;

    @Schema(example = "4962187580551")
    @NotBlank(message = "El número de operación es obligatorio")
    private String numeroOperacion;

    @Schema(description = "Ruta o URL de la captura/voucher de la transferencia")
    private String comprobanteUrl;

}