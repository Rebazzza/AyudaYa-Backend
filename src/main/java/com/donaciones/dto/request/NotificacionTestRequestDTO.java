package com.donaciones.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificacionTestRequestDTO {

    @NotBlank(message = "El correo destino es obligatorio")
    @Email(message = "El correo destino no tiene un formato válido")
    private String correoDestino;

    @NotBlank(message = "El nombre del donante es obligatorio")
    private String nombreDonante;

    @NotBlank(message = "El código de seguimiento es obligatorio")
    private String codigoSeguimiento;

    @NotBlank(message = "El nuevo estado es obligatorio")
    private String nuevoEstado;

    private String nombreLocal;

}