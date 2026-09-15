package com.donaciones.repository;

import com.donaciones.entity.DetalleDonacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DetalleDonacionRepository extends JpaRepository<DetalleDonacion, Integer> {

    List<DetalleDonacion> findByDonacionIdDonacion(Integer idDonacion);

    List<DetalleDonacion> findByDonacionEstadoActualAndDonacionLocalRecepcionIdLocal(String estado, Long idLocal);

    List<DetalleDonacion> findByDonacionEstadoActualAndDonacionLocalRecepcionIdLocalAndCategoriaIdCategoria(
            String estado, Long idLocal, Integer idCategoria);

}