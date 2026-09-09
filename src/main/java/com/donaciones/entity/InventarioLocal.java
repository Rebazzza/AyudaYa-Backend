package com.donaciones.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
@Table(name = "inventario_locales", uniqueConstraints = {
        @UniqueConstraint(name = "uk_inventario_local_categoria", columnNames = {"id_local", "id_tipo_categoria_item"})
})
public class InventarioLocal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_inventario_local")
    private Long idInventarioLocal;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_local", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private LocalRecepcion local;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_tipo_categoria_item", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private TipoCategoriaItem tipoCategoriaItem;

    @Column(name = "cantidad_disponible", nullable = false)
    private Double cantidadDisponible;

    @Column(name = "ultima_actualizacion", nullable = false)
    private LocalDateTime ultimaActualizacion;

}