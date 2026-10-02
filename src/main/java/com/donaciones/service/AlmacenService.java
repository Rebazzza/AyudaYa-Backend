package com.donaciones.service;

import com.donaciones.dto.request.CorroboracionRequestDTO;
import com.donaciones.dto.response.AlertaCaducidadDTO;
import com.donaciones.dto.response.DonacionResponse;
import com.donaciones.dto.response.PaginaDTO;
import com.donaciones.dto.response.ProductoInventarioDTO;
import com.donaciones.dto.response.ResumenInventarioDTO;

import java.util.List;

public interface AlmacenService {

    DonacionResponse corroborarRecepcion(CorroboracionRequestDTO dto);

    List<ResumenInventarioDTO> obtenerInventarioPorLocal(Long idLocal);

    List<AlertaCaducidadDTO> obtenerAlertasCaducidad(Long idLocal);

    PaginaDTO<ProductoInventarioDTO> buscarProductosInventario(
            Long idLocal, String busqueda, Integer idCategoria, String estadoConservacion, int pagina, int tamanio);

}