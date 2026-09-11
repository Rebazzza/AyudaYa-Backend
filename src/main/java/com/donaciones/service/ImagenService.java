package com.donaciones.service;

import com.donaciones.dto.response.ImagenResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ImagenService {

    ImagenResponseDTO guardarImagen(MultipartFile archivo, String tipoImagen, Integer idDonacion, Long idUsuario);

    List<ImagenResponseDTO> guardarEvidenciasDonacion(List<MultipartFile> archivos, Integer idDonacion);

    List<ImagenResponseDTO> listarImagenesPorDonacion(Integer idDonacion);

    void eliminarImagen(Integer idImagen);

}