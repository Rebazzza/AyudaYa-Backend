package com.donaciones.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "categoriainsumo")
public class CategoriaInsumo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_Categoria")
    private Integer idCategoria;

    @Column(name = "nombreCategoria", nullable = false, length = 50)
    private String nombreCategoria;

    @Column(name = "unidadMedCate", nullable = false, length = 20)
    private String unidadMedCate;

    @Column(name = "refrigerar", nullable = false)
    private Boolean refrigerar;

    @PrePersist
    public void prePersist() {
        if (refrigerar == null) {
            refrigerar = false;
        }
    }

}