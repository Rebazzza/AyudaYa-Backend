package com.donaciones.controller;

import com.donaciones.dto.ApiResponseDTO;
import com.donaciones.dto.request.LoginRequest;
import com.donaciones.dto.request.RegisterRequest;
import com.donaciones.dto.response.UsuarioResponse;
import com.donaciones.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Registro e inicio de sesión de usuarios")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Registrar un usuario donante",
            description = "Registra un nuevo usuario asignándole el rol 'Donante'. Valida DNI de 8 dígitos y correo único.")
    public ResponseEntity<ApiResponseDTO<UsuarioResponse>> register(@Valid @RequestBody RegisterRequest request,
                                                                   HttpServletRequest servletRequest) {
        UsuarioResponse response = authService.register(request);
        ApiResponseDTO<UsuarioResponse> body = ApiResponseDTO.success(HttpStatus.CREATED,
                "Usuario registrado exitosamente", servletRequest.getRequestURI(), response);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión",
            description = "Valida las credenciales del usuario contra el correo y la contraseña encriptada.")
    public ResponseEntity<ApiResponseDTO<UsuarioResponse>> login(@Valid @RequestBody LoginRequest request,
                                                                 HttpServletRequest servletRequest) {
        UsuarioResponse response = authService.login(request);
        ApiResponseDTO<UsuarioResponse> body = ApiResponseDTO.success(HttpStatus.OK,
                "Inicio de sesión exitoso", servletRequest.getRequestURI(), response);
        return ResponseEntity.ok(body);
    }

}