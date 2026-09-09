package com.donaciones.entity;

import com.donaciones.enums.EstadoDonacion;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
@Table(name = "donaciones")
public class Donacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_donacion")
    private Long idDonacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario_donante", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Usuario donante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_local_destino", nullable = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private LocalRecepcion localDestino;

    @Column(name = "fecha_donacion", nullable = false, updatable = false)
    private LocalDateTime fechaDonacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoDonacion estado;

    @Column(name = "descripcion_general", nullable = false, length = 500)
    private String descripcionGeneral;

    @Builder.Default
    @OneToMany(mappedBy = "donacion", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<ItemDonacion> items = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "donacion", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<EstadoDonacionHistory> historialEstados = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "donacion", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<LogisticaEnvio> envios = new ArrayList<>();

    public void addItem(ItemDonacion item) {
        items.add(item);
        item.setDonacion(this);
    }

    public void removeItem(ItemDonacion item) {
        items.remove(item);
        item.setDonacion(null);
    }

}