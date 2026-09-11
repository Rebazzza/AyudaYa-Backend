package com.donaciones.service;

import com.donaciones.dto.request.LocalRequest;
import com.donaciones.dto.response.LocalResponse;
import com.donaciones.entity.LocalRecepcion;
import com.donaciones.exception.ResourceNotFoundException;
import com.donaciones.repository.LocalRecepcionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LocalService {

    private final LocalRecepcionRepository localRepository;

    public List<LocalResponse> listActive() {
        return localRepository.findByEstadoActivoTrue().stream()
                .map(this::toResponse)
                .toList();
    }

    public LocalResponse getById(Long id) {
        LocalRecepcion local = localRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el local con id " + id));
        return toResponse(local);
    }

    @Transactional
    public LocalResponse create(LocalRequest request) {
        LocalRecepcion local = LocalRecepcion.builder()
                .nombreLocal(request.getNombreLocal())
                .direccionLocal(request.getDireccionLocal())
                .latitud(request.getLatitud())
                .longitud(request.getLongitud())
                .capacidadLocalM3(request.getCapacidadLocalM3())
                .telefonoLocal(request.getTelefonoLocal())
                .estadoActivo(true)
                .build();
        return toResponse(localRepository.save(local));
    }

    @Transactional
    public LocalResponse update(Long id, LocalRequest request) {
        LocalRecepcion local = findByIdOrThrow(id);
        local.setNombreLocal(request.getNombreLocal());
        local.setDireccionLocal(request.getDireccionLocal());
        local.setLatitud(request.getLatitud());
        local.setLongitud(request.getLongitud());
        local.setCapacidadLocalM3(request.getCapacidadLocalM3());
        local.setTelefonoLocal(request.getTelefonoLocal());
        return toResponse(localRepository.save(local));
    }

    @Transactional
    public void delete(Long id) {
        LocalRecepcion local = findByIdOrThrow(id);
        localRepository.delete(local);
    }

    private LocalRecepcion findByIdOrThrow(Long id) {
        return localRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el local con id " + id));
    }

    private LocalResponse toResponse(LocalRecepcion local) {
        return LocalResponse.builder()
                .idLocal(local.getIdLocal())
                .nombreLocal(local.getNombreLocal())
                .direccionLocal(local.getDireccionLocal())
                .latitud(local.getLatitud())
                .longitud(local.getLongitud())
                .capacidadLocalM3(local.getCapacidadLocalM3())
                .telefonoLocal(local.getTelefonoLocal())
                .estadoActivo(local.getEstadoActivo())
                .build();
    }

}