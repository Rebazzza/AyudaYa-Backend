package com.donaciones.service;

import com.donaciones.dto.request.LoginRequest;
import com.donaciones.dto.request.RegisterRequest;
import com.donaciones.dto.response.UsuarioResponse;
import com.donaciones.entity.Rol;
import com.donaciones.entity.Usuario;
import com.donaciones.enums.RolRegistrable;
import com.donaciones.exception.DuplicateResourceException;
import com.donaciones.exception.InvalidCredentialsException;
import com.donaciones.exception.ResourceNotFoundException;
import com.donaciones.repository.RolRepository;
import com.donaciones.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UsuarioResponse register(RegisterRequest request) {
        if (usuarioRepository.existsByCorreoUsuario(request.getCorreoUsuario())) {
            throw new DuplicateResourceException(
                    "Ya existe un usuario registrado con el correo " + request.getCorreoUsuario());
        }
        if (usuarioRepository.existsByDniUsuario(request.getDniUsuario())) {
            throw new DuplicateResourceException(
                    "Ya existe un usuario registrado con el DNI " + request.getDniUsuario());
        }

        RolRegistrable tipoRegistro = request.getTipoRegistro() != null
                ? request.getTipoRegistro()
                : RolRegistrable.DONANTE;

        Rol rol = rolRepository.findByNombreRol(tipoRegistro.getNombreRol())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "El rol " + tipoRegistro.getNombreRol() + " no está configurado en el sistema"));

        Usuario usuario = Usuario.builder()
                .rol(rol)
                .dniUsuario(request.getDniUsuario())
                .nombreUsuario(request.getNombreUsuario())
                .apellidosUsuario(request.getApellidosUsuario())
                .correoUsuario(request.getCorreoUsuario())
                .contraseña(passwordEncoder.encode(request.getContraseña()))
                .telefonoUsuario(request.getTelefonoUsuario())
                .build();

        return toResponse(usuarioRepository.save(usuario));
    }

    public UsuarioResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByCorreoUsuario(request.getCorreoUsuario())
                .orElseThrow(() -> new InvalidCredentialsException("Credenciales incorrectas"));

        if (!passwordEncoder.matches(request.getContraseña(), usuario.getContraseña())) {
            throw new InvalidCredentialsException("Credenciales incorrectas");
        }

        return toResponse(usuario);
    }

    private UsuarioResponse toResponse(Usuario usuario) {
        return UsuarioResponse.builder()
                .idUsuario(usuario.getIdUsuario())
                .dniUsuario(usuario.getDniUsuario())
                .nombreUsuario(usuario.getNombreUsuario())
                .apellidosUsuario(usuario.getApellidosUsuario())
                .correoUsuario(usuario.getCorreoUsuario())
                .telefonoUsuario(usuario.getTelefonoUsuario())
                .nombreRol(usuario.getRol().getNombreRol())
                .fechaRegistro(usuario.getFechaRegistro())
                .build();
    }

}