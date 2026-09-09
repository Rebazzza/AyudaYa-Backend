package com.donaciones.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "donacion")
public class Donacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_Donacion")
    private Integer idDonacion;

    @Column(name = "codigoSeguimiento", nullable = false, unique = true, length = 15)
    private String codigoSeguimiento;

    @Column(name = "fechaRegistro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "fechaExpiracion")
    private LocalDateTime fechaExpiracion;

    @Column(name = "fechaVerificacion")
    private LocalDateTime fechaVerificacion;

    @Column(name = "estadoActual", nullable = false, length = 25)
    private String estadoActual;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_Usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_LocalRecepcion", nullable = false)
    private LocalRecepcion localRecepcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_Trabajador", nullable = true)
    private Trabajador trabajador;

    @Builder.Default
    @OneToMany(mappedBy = "donacion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleDonacion> detalles = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (fechaRegistro == null) {
            fechaRegistro = LocalDateTime.now();
        }
    }

    public void addDetalle(DetalleDonacion detalle) {
        detalles.add(detalle);
        detalle.setDonacion(this);
    }

    public void removeDetalle(DetalleDonacion detalle) {
        detalles.remove(detalle);
        detalle.setDonacion(null);
    }

}