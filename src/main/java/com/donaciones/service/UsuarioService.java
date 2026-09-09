package com.donaciones.service;

import com.donaciones.dto.request.UsuarioUpdateRequest;
import com.donaciones.dto.response.UsuarioResponse;
import com.donaciones.entity.Rol;
import com.donaciones.entity.Usuario;
import com.donaciones.exception.DuplicateResourceException;
import com.donaciones.exception.ResourceNotFoundException;
import com.donaciones.repository.RolRepository;
import com.donaciones.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public List<UsuarioResponse> list() {
        return usuarioRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public UsuarioResponse getById(Long id) {
        return toResponse(findByIdOrThrow(id));
    }

    @Transactional
    public UsuarioResponse update(Long id, UsuarioUpdateRequest request) {
        Usuario usuario = findByIdOrThrow(id);

        if (usuarioRepository.existsByCorreoUsuarioAndIdUsuarioNot(request.getCorreoUsuario(), id)) {
            throw new DuplicateResourceException(
                    "Ya existe otro usuario registrado con el correo " + request.getCorreoUsuario());
        }
        if (usuarioRepository.existsByDniUsuarioAndIdUsuarioNot(request.getDniUsuario(), id)) {
            throw new DuplicateResourceException(
                    "Ya existe otro usuario registrado con el DNI " + request.getDniUsuario());
        }

        usuario.setDniUsuario(request.getDniUsuario());
        usuario.setNombreUsuario(request.getNombreUsuario());
        usuario.setApellidosUsuario(request.getApellidosUsuario());
        usuario.setCorreoUsuario(request.getCorreoUsuario());
        usuario.setTelefonoUsuario(request.getTelefonoUsuario());

        if (request.getContraseña() != null && !request.getContraseña().isBlank()) {
            usuario.setContraseña(passwordEncoder.encode(request.getContraseña()));
        }

        if (request.getTipoRegistro() != null) {
            Rol nuevoRol = rolRepository.findByNombreRol(request.getTipoRegistro().getNombreRol())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "El rol " + request.getTipoRegistro().getNombreRol() + " no está configurado en el sistema"));
            usuario.setRol(nuevoRol);
        }

        return toResponse(usuarioRepository.save(usuario));
    }

    private Usuario findByIdOrThrow(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el usuario con id " + id));
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