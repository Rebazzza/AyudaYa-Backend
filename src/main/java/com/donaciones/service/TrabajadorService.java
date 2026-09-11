package com.donaciones.service;

import com.donaciones.dto.request.TrabajadorRequest;
import com.donaciones.dto.response.TrabajadorResponse;
import com.donaciones.entity.LocalRecepcion;
import com.donaciones.entity.Trabajador;
import com.donaciones.entity.Usuario;
import com.donaciones.exception.DuplicateResourceException;
import com.donaciones.exception.ResourceNotFoundException;
import com.donaciones.repository.LocalRecepcionRepository;
import com.donaciones.repository.TrabajadorRepository;
import com.donaciones.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TrabajadorService {

    private final TrabajadorRepository trabajadorRepository;
    private final UsuarioRepository usuarioRepository;
    private final LocalRecepcionRepository localRepository;

    @Transactional(readOnly = true)
    public List<TrabajadorResponse> list() {
        return trabajadorRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TrabajadorResponse getById(Integer id) {
        return toResponse(findByIdOrThrow(id));
    }

    @Transactional
    public TrabajadorResponse create(TrabajadorRequest request) {
        if (trabajadorRepository.existsByUsuarioIdUsuario(request.getIdUsuario())) {
            throw new DuplicateResourceException("El usuario ya se encuentra registrado como trabajador");
        }
        Usuario usuario = findUsuario(request.getIdUsuario());
        LocalRecepcion local = findLocal(request.getIdLocal());

        Trabajador trabajador = Trabajador.builder()
                .usuario(usuario)
                .localRecepcion(local)
                .cargoTrabajador(request.getCargoTrabajador())
                .fechaContratacion(request.getFechaContratacion() != null
                        ? request.getFechaContratacion()
                        : LocalDateTime.now())
                .build();
        return toResponse(trabajadorRepository.save(trabajador));
    }

    @Transactional
    public TrabajadorResponse update(Integer id, TrabajadorRequest request) {
        Trabajador trabajador = findByIdOrThrow(id);
        if (trabajadorRepository.existsByUsuarioIdUsuarioAndIdTrabajadorNot(request.getIdUsuario(), id)) {
            throw new DuplicateResourceException("El usuario ya se encuentra registrado como trabajador");
        }

        trabajador.setUsuario(findUsuario(request.getIdUsuario()));
        trabajador.setLocalRecepcion(findLocal(request.getIdLocal()));
        trabajador.setCargoTrabajador(request.getCargoTrabajador());
        if (request.getFechaContratacion() != null) {
            trabajador.setFechaContratacion(request.getFechaContratacion());
        }
        return toResponse(trabajadorRepository.save(trabajador));
    }

    @Transactional
    public void delete(Integer id) {
        Trabajador trabajador = findByIdOrThrow(id);
        trabajadorRepository.delete(trabajador);
    }

    private Trabajador findByIdOrThrow(Integer id) {
        return trabajadorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el trabajador con id " + id));
    }

    private Usuario findUsuario(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el usuario con id " + id));
    }

    private LocalRecepcion findLocal(Long id) {
        return localRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el local con id " + id));
    }

    private TrabajadorResponse toResponse(Trabajador trabajador) {
        return TrabajadorResponse.builder()
                .idTrabajador(trabajador.getIdTrabajador())
                .cargoTrabajador(trabajador.getCargoTrabajador())
                .fechaContratacion(trabajador.getFechaContratacion())
                .idUsuario(trabajador.getUsuario().getIdUsuario())
                .dniUsuario(trabajador.getUsuario().getDniUsuario())
                .nombreCompletoUsuario(trabajador.getUsuario().getNombreUsuario() + " "
                        + trabajador.getUsuario().getApellidosUsuario())
                .idLocal(trabajador.getLocalRecepcion().getIdLocal())
                .nombreLocal(trabajador.getLocalRecepcion().getNombreLocal())
                .build();
    }

}