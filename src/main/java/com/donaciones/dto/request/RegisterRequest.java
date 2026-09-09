package com.donaciones.dto.request;

import com.donaciones.enums.RolRegistrable;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Solicitud de registro de un usuario")
public class RegisterRequest {

    @Schema(example = "DONANTE", description = "Tipo de registro: DONANTE o PERSONAL_APOYO. Si se omite, se asume DONANTE.")
    @NotNull(message = "El tipo de registro debe ser DONANTE o PERSONAL_APOYO")
    private RolRegistrable tipoRegistro;

    @Schema(example = "12345678")
    @NotBlank(message = "El DNI es obligatorio")
    @Pattern(regexp = "\\d{8}", message = "El DNI debe contener exactamente 8 dígitos")
    private String dniUsuario;

    @Schema(example = "María")
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 50, message = "El nombre no debe superar los 50 caracteres")
    private String nombreUsuario;

    @Schema(example = "García Pérez")
    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(max = 100, message = "Los apellidos no deben superar los 100 caracteres")
    private String apellidosUsuario;

    @Schema(example = "maria.garcia@example.com")
    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no tiene un formato válido")
    @Size(max = 100, message = "El correo no debe superar los 100 caracteres")
    private String correoUsuario;

    @Schema(example = "Password123")
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, max = 100, message = "La contraseña debe tener entre 8 y 100 caracteres")
    private String contraseña;

    @Schema(example = "987654321")
    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "\\d{9}", message = "El teléfono debe contener exactamente 9 dígitos")
    private String telefonoUsuario;

}