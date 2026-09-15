package com.donaciones.service;

import com.donaciones.dto.request.RegistroMonetarioRequestDTO;
import com.donaciones.dto.response.DonacionMonetariaResponseDTO;
import com.donaciones.dto.response.TotalFondosDTO;

public interface DonacionMonetariaService {

    DonacionMonetariaResponseDTO registrarDonacionMonetaria(RegistroMonetarioRequestDTO dto);

    DonacionMonetariaResponseDTO verificarFondos(Integer idDonacionMonetaria, String estado);

    TotalFondosDTO obtenerTotalFondosRecaudados();

}