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
@Table(name = "kit")
public class Kit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_Kit")
    private Integer idKit;

    @Column(name = "codigoKit", nullable = false, unique = true, length = 15)
    private String codigoKit;

    @Column(name = "nombreKit", nullable = false, length = 100)
    private String nombreKit;

    @Column(name = "estado", nullable = false, length = 25)
    private String estado;

    @Column(name = "fechaCreacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_Local", nullable = false)
    private LocalRecepcion localRecepcion;

    @OneToMany(mappedBy = "kit", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleKit> detalles = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (estado == null) {
            estado = "DISPONIBLE";
        }
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
    }

    public void agregarDetalle(DetalleKit detalle) {
        detalle.setKit(this);
        this.detalles.add(detalle);
    }

}