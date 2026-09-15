package com.donaciones.service;

import com.donaciones.dto.request.CrearKitRequestDTO;
import com.donaciones.dto.response.KitResponseDTO;

import java.util.List;

public interface KitService {

    List<KitResponseDTO> armarKits(CrearKitRequestDTO dto);

    List<KitResponseDTO> listarKitsPorLocal(Long idLocal);

    KitResponseDTO obtenerKitPorCodigo(String codigoKit);

}