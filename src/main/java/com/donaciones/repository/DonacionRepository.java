package com.donaciones.repository;

import com.donaciones.entity.Donacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DonacionRepository extends JpaRepository<Donacion, Integer> {

    boolean existsByCodigoSeguimiento(String codigoSeguimiento);

    boolean existsByCodigoSeguimientoAndIdDonacionNot(String codigoSeguimiento, Integer idDonacion);

    Optional<Donacion> findByCodigoSeguimiento(String codigoSeguimiento);

    List<Donacion> findByUsuarioIdUsuario(Long idUsuario);

    List<Donacion> findByUsuarioIdUsuarioOrderByFechaRegistroDesc(Long idUsuario);

    List<Donacion> findByEstadoActual(String estado);

    List<Donacion> findByUsuarioIdUsuarioAndEstadoActual(Long idUsuario, String estado);

    long countByLocalRecepcionIdLocalAndEstadoActual(Long idLocal, String estado);

}