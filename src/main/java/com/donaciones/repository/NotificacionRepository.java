package com.donaciones.repository;

import com.donaciones.entity.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificacionRepository extends JpaRepository<Notificacion, Integer> {

    List<Notificacion> findAllByOrderByFechaEnvioDesc();

    List<Notificacion> findByUsuarioIdUsuarioOrderByFechaEnvioDesc(Long idUsuario);

    List<Notificacion> findByUsuarioIdUsuarioAndLeidoFalse(Long idUsuario);

}