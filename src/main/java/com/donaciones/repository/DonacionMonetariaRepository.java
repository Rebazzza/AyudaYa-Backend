package com.donaciones.repository;

import com.donaciones.entity.DonacionMonetaria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DonacionMonetariaRepository extends JpaRepository<DonacionMonetaria, Integer> {

    List<DonacionMonetaria> findByEstadoVerificacion(String estadoVerificacion);

    Optional<DonacionMonetaria> findByNumeroOperacion(String numeroOperacion);

}