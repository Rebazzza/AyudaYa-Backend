package com.donaciones.service;

import com.donaciones.dto.request.HistorialEstadoRequest;
import com.donaciones.dto.response.HistorialEstadoResponse;
import com.donaciones.entity.Donacion;
import com.donaciones.entity.HistorialEstado;
import com.donaciones.entity.LocalRecepcion;
import com.donaciones.entity.Trabajador;
import com.donaciones.exception.ResourceNotFoundException;
import com.donaciones.repository.DonacionRepository;
import com.donaciones.repository.HistorialEstadoRepository;
import com.donaciones.repository.LocalRecepcionRepository;
import com.donaciones.repository.TrabajadorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HistorialEstadoService {

    private final HistorialEstadoRepository historialRepository;
    private final DonacionRepository donacionRepository;
    private final LocalRecepcionRepository localRepository;
    private final TrabajadorRepository trabajadorRepository;

    @Transactional(readOnly = true)
    public List<HistorialEstadoResponse> list() {
        return historialRepository.findAllByOrderByFechaCambioDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<HistorialEstadoResponse> listByDonacion(Integer idDonacion) {
        return historialRepository.findByDonacionIdDonacionOrderByFechaCambioDesc(idDonacion).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public HistorialEstadoResponse getById(Integer id) {
        return toResponse(findByIdOrThrow(id));
    }

    @Transactional
    public HistorialEstadoResponse create(HistorialEstadoRequest request) {
        Donacion donacion = donacionRepository.findById(request.getIdDonacion())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró la donación con id " + request.getIdDonacion()));
        HistorialEstado historial = HistorialEstado.builder()
                .donacion(donacion)
                .estado(request.getEstado())
                .observacionHistorial(request.getObservacionHistorial())
                .localRecepcion(resolveLocal(request.getIdLocal()))
                .trabajador(resolveTrabajador(request.getIdTrabajador()))
                .build();
        return toResponse(historialRepository.save(historial));
    }

    @Transactional
    public void delete(Integer id) {
        HistorialEstado historial = findByIdOrThrow(id);
        historialRepository.delete(historial);
    }

    private HistorialEstado findByIdOrThrow(Integer id) {
        return historialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el historial con id " + id));
    }

    private LocalRecepcion resolveLocal(Long id) {
        if (id == null) {
            return null;
        }
        return localRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el local con id " + id));
    }

    private Trabajador resolveTrabajador(Integer id) {
        if (id == null) {
            return null;
        }
        return trabajadorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el trabajador con id " + id));
    }

    private HistorialEstadoResponse toResponse(HistorialEstado historial) {
        return HistorialEstadoResponse.builder()
                .idHistorial(historial.getIdHistorial())
                .idDonacion(historial.getDonacion().getIdDonacion())
                .codigoSeguimiento(historial.getDonacion().getCodigoSeguimiento())
                .estado(historial.getEstado())
                .fechaCambio(historial.getFechaCambio())
                .observacionHistorial(historial.getObservacionHistorial())
                .idLocal(historial.getLocalRecepcion() != null
                        ? historial.getLocalRecepcion().getIdLocal() : null)
                .nombreLocal(historial.getLocalRecepcion() != null
                        ? historial.getLocalRecepcion().getNombreLocal() : null)
                .idTrabajador(historial.getTrabajador() != null
                        ? historial.getTrabajador().getIdTrabajador() : null)
                .nombreTrabajador(historial.getTrabajador() != null
                        ? historial.getTrabajador().getUsuario().getNombreUsuario() + " "
                        + historial.getTrabajador().getUsuario().getApellidosUsuario()
                        : null)
                .build();
    }

}