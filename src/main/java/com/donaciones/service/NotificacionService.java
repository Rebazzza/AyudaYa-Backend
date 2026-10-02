package com.donaciones.service;

import com.donaciones.dto.request.NotificacionRequest;
import com.donaciones.dto.response.NotificacionResponse;
import com.donaciones.entity.Donacion;
import com.donaciones.entity.Notificacion;
import com.donaciones.entity.Usuario;
import com.donaciones.exception.ResourceNotFoundException;
import com.donaciones.repository.DonacionRepository;
import com.donaciones.repository.NotificacionRepository;
import com.donaciones.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final DonacionRepository donacionRepository;

    @Transactional(readOnly = true)
    public List<NotificacionResponse> list(Long idUsuario) {
        List<Notificacion> notificaciones = idUsuario != null
                ? notificacionRepository.findByUsuarioIdUsuarioOrderByFechaEnvioDesc(idUsuario)
                : notificacionRepository.findAllByOrderByFechaEnvioDesc();
        return notificaciones.stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public NotificacionResponse getById(Integer id) {
        return toResponse(findByIdOrThrow(id));
    }

    @Transactional
    public NotificacionResponse create(NotificacionRequest request) {
        Usuario usuario = usuarioRepository.findById(request.getIdUsuario())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró el usuario con id " + request.getIdUsuario()));
        Donacion donacion = null;
        if (request.getIdDonacion() != null) {
            donacion = donacionRepository.findById(request.getIdDonacion())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "No se encontró la donación con id " + request.getIdDonacion()));
        }
        Notificacion notificacion = Notificacion.builder()
                .usuario(usuario)
                .donacion(donacion)
                .mensajeNoti(request.getMensajeNoti())
                .build();
        return toResponse(notificacionRepository.save(notificacion));
    }

    @Transactional
    public NotificacionResponse marcarLeido(Integer id) {
        Notificacion notificacion = findByIdOrThrow(id);
        notificacion.setLeido(true);
        return toResponse(notificacionRepository.save(notificacion));
    }

    @Transactional
    public int marcarTodoLeido(Long idUsuario) {
        List<Notificacion> pendientes = notificacionRepository.findByUsuarioIdUsuarioAndLeidoFalse(idUsuario);
        if (pendientes.isEmpty()) {
            return 0;
        }
        pendientes.forEach(notificacion -> notificacion.setLeido(true));
        notificacionRepository.saveAll(pendientes);
        return pendientes.size();
    }

    @Transactional
    public void delete(Integer id) {
        Notificacion notificacion = findByIdOrThrow(id);
        notificacionRepository.delete(notificacion);
    }

    private Notificacion findByIdOrThrow(Integer id) {
        return notificacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la notificación con id " + id));
    }

    private NotificacionResponse toResponse(Notificacion notificacion) {
        return NotificacionResponse.builder()
                .idNotificacion(notificacion.getIdNotificacion())
                .idUsuario(notificacion.getUsuario().getIdUsuario())
                .idDonacion(notificacion.getDonacion() != null
                        ? notificacion.getDonacion().getIdDonacion() : null)
                .mensajeNoti(notificacion.getMensajeNoti())
                .fechaEnvio(notificacion.getFechaEnvio())
                .leido(notificacion.getLeido())
                .build();
    }

}