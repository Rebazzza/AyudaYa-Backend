package com.donaciones.repository;

import com.donaciones.entity.HistorialEstado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistorialEstadoRepository extends JpaRepository<HistorialEstado, Integer> {

    List<HistorialEstado> findAllByOrderByFechaCambioDesc();

    List<HistorialEstado> findByDonacionIdDonacionOrderByFechaCambioDesc(Integer idDonacion);

    List<HistorialEstado> findByDonacionIdDonacionOrderByFechaCambioAsc(Integer idDonacion);

}