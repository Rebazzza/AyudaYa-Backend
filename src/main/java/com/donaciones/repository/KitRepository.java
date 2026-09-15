package com.donaciones.repository;

import com.donaciones.entity.Kit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface KitRepository extends JpaRepository<Kit, Integer> {

    Optional<Kit> findByCodigoKit(String codigoKit);

    List<Kit> findByLocalRecepcionIdLocal(Long idLocal);

    boolean existsByCodigoKit(String codigoKit);

}