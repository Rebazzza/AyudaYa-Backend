package com.donaciones.service;

import com.donaciones.dto.request.ActualizarEstadoRequest;
import com.donaciones.dto.request.ActualizarUbicacionRequestDTO;
import com.donaciones.dto.request.DonacionRegistroRequestDTO;
import com.donaciones.dto.request.DonacionRequest;
import com.donaciones.dto.response.DonacionResponse;
import com.donaciones.dto.response.DonacionResponseDTO;
import com.donaciones.dto.response.TrackingResponseDTO;
import com.donaciones.dto.response.UbicacionActualResponseDTO;

import java.util.List;

public interface DonacionService {

    List<DonacionResponse> list(Integer idUsuario, String estado);

    DonacionResponse getById(Integer id);

    DonacionResponse getByCodigo(String codigoSeguimiento);

    DonacionResponse update(Integer id, DonacionRequest request);

    DonacionResponse cambiarEstado(Integer id, ActualizarEstadoRequest request);

    void delete(Integer id);

    DonacionResponseDTO registrarDonacion(DonacionRegistroRequestDTO dto);

    TrackingResponseDTO obtenerSeguimiento(String codigoSeguimiento);

    List<DonacionResponseDTO> obtenerDonacionesPorUsuario(Long idUsuario);

    DonacionResponseDTO anularDonacion(Integer idDonacion, Long idUsuario);

    UbicacionActualResponseDTO actualizarUbicacionYEstado(ActualizarUbicacionRequestDTO dto);

    UbicacionActualResponseDTO obtenerUbicacionActual(String codigoSeguimiento);

}