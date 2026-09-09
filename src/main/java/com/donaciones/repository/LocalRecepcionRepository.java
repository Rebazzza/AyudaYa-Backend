package com.donaciones.repository;

import com.donaciones.entity.LocalRecepcion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LocalRecepcionRepository extends JpaRepository<LocalRecepcion, Long> {

    List<LocalRecepcion> findByEstadoActivoTrue();

}