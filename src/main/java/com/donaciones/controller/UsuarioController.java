package com.donaciones.controller;

import com.donaciones.dto.ApiResponseDTO;
import com.donaciones.dto.request.RegisterRequest;
import com.donaciones.dto.request.UsuarioUpdateRequest;
import com.donaciones.dto.response.UsuarioResponse;
import com.donaciones.service.AuthService;
import com.donaciones.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "Gestión de usuarios del sistema")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final AuthService authService;

    @GetMapping
    @Operation(summary = "Listar usuarios", description = "Devuelve todos los usuarios registrados.")
    public ResponseEntity<ApiResponseDTO<List<UsuarioResponse>>> list(HttpServletRequest servletRequest) {
        List<UsuarioResponse> usuarios = usuarioService.list();
        ApiResponseDTO<List<UsuarioResponse>> body = ApiResponseDTO.success(HttpStatus.OK,
                "Usuarios obtenidos exitosamente", servletRequest.getRequestURI(), usuarios);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener usuario por ID", description = "Devuelve el detalle de un usuario.")
    public ResponseEntity<ApiResponseDTO<UsuarioResponse>> getById(@PathVariable Long id,
                                                                   HttpServletRequest servletRequest) {
        UsuarioResponse usuario = usuarioService.getById(id);
        ApiResponseDTO<UsuarioResponse> body = ApiResponseDTO.success(HttpStatus.OK,
                "Usuario obtenido exitosamente", servletRequest.getRequestURI(), usuario);
        return ResponseEntity.ok(body);
    }

    @PostMapping
    @Operation(summary = "Registrar usuario", description = "Crea un usuario asignándole el rol según tipoRegistro (DONANTE o PERSONAL_APOYO).")
    public ResponseEntity<ApiResponseDTO<UsuarioResponse>> create(@Valid @RequestBody RegisterRequest request,
                                                                  HttpServletRequest servletRequest) {
        UsuarioResponse usuario = authService.register(request);
        ApiResponseDTO<UsuarioResponse> body = ApiResponseDTO.success(HttpStatus.CREATED,
                "Usuario creado exitosamente", servletRequest.getRequestURI(), usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar usuario", description = "Actualiza datos, contraseña y rol de un usuario existente.")
    public ResponseEntity<ApiResponseDTO<UsuarioResponse>> update(@PathVariable Long id,
                                                                  @Valid @RequestBody UsuarioUpdateRequest request,
                                                                  HttpServletRequest servletRequest) {
        UsuarioResponse usuario = usuarioService.update(id, request);
        ApiResponseDTO<UsuarioResponse> body = ApiResponseDTO.success(HttpStatus.OK,
                "Usuario actualizado exitosamente", servletRequest.getRequestURI(), usuario);
        return ResponseEntity.ok(body);
    }

}