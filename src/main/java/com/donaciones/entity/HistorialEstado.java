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
@Table(name = "historialestado")
public class HistorialEstado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_Historial")
    private Integer idHistorial;

    @Column(name = "estado", nullable = false, length = 25)
    private String estado;

    @Column(name = "fechaCambio", nullable = false, updatable = false)
    private LocalDateTime fechaCambio;

    @Column(name = "observacionHistorial", length = 255)
    private String observacionHistorial;

    @Column(name = "latitud")
    private Double latitud;

    @Column(name = "longitud")
    private Double longitud;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_Donacion", nullable = false)
    private Donacion donacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_Local", nullable = true)
    private LocalRecepcion localRecepcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_Trabajador", nullable = true)
    private Trabajador trabajador;

    @PrePersist
    public void prePersist() {
        if (fechaCambio == null) {
            fechaCambio = LocalDateTime.now();
        }
    }

}