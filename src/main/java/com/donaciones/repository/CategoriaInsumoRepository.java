package com.donaciones.repository;

import com.donaciones.entity.CategoriaInsumo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoriaInsumoRepository extends JpaRepository<CategoriaInsumo, Integer> {

    boolean existsByNombreCategoria(String nombreCategoria);

    boolean existsByNombreCategoriaAndIdCategoriaNot(String nombreCategoria, Integer idCategoria);

    List<CategoriaInsumo> findByActivoTrue();

    List<CategoriaInsumo> findByActivoFalse();

}