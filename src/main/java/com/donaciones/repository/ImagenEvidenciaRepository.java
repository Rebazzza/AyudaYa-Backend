package com.donaciones.repository;

import com.donaciones.entity.ImagenEvidencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImagenEvidenciaRepository extends JpaRepository<ImagenEvidencia, Integer> {

    List<ImagenEvidencia> findByDonacionIdDonacion(Integer idDonacion);

    List<ImagenEvidencia> findByUsuarioIdUsuario(Long idUsuario);

}