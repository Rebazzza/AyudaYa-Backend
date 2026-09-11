package com.donaciones.repository;

import com.donaciones.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByCorreoUsuario(String correoUsuario);

    boolean existsByCorreoUsuario(String correoUsuario);

    boolean existsByDniUsuario(String dniUsuario);

    boolean existsByCorreoUsuarioAndIdUsuarioNot(String correoUsuario, Long idUsuario);

    boolean existsByDniUsuarioAndIdUsuarioNot(String dniUsuario, Long idUsuario);

    List<Usuario> findByRolNombreRol(String nombreRol);

}