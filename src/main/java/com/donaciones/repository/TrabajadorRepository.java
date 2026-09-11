package com.donaciones.repository;

import com.donaciones.entity.Trabajador;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrabajadorRepository extends JpaRepository<Trabajador, Integer> {

    boolean existsByUsuarioIdUsuario(Long idUsuario);

    boolean existsByUsuarioIdUsuarioAndIdTrabajadorNot(Long idUsuario, Integer idTrabajador);

}