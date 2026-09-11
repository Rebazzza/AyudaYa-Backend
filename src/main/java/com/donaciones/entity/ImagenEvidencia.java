package com.donaciones.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "imagenevidencia")
public class ImagenEvidencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_Imagen")
    private Integer idImagen;

    @Column(name = "nombreArchivo", nullable = false, length = 255)
    private String nombreArchivo;

    @Column(name = "nombreOriginal", nullable = false, length = 255)
    private String nombreOriginal;

    @Column(name = "rutaUrl", nullable = false, length = 500)
    private String rutaUrl;

    @Column(name = "tipoImagen", nullable = false, length = 50)
    private String tipoImagen;

    @Column(name = "tamanoBytes")
    private Long tamanoBytes;

    @Column(name = "fechaCarga", nullable = false, updatable = false)
    private LocalDateTime fechaCarga;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_Donacion")
    private Donacion donacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_Usuario")
    private Usuario usuario;

    @PrePersist
    public void prePersist() {
        if (fechaCarga == null) {
            fechaCarga = LocalDateTime.now();
        }
    }

}