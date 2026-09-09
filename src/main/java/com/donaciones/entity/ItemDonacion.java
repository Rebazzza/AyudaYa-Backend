package com.donaciones.entity;

import com.donaciones.enums.UnidadMedida;
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
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
@Table(name = "items_donacion")
public class ItemDonacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_item_donacion")
    private Long idItemDonacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_donacion", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Donacion donacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_tipo_categoria_item", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private TipoCategoriaItem tipoCategoriaItem;

    @Column(name = "nombre_item", nullable = false, length = 100)
    private String nombreItem;

    @Column(name = "detalle", length = 255)
    private String detalle;

    @Column(name = "cantidad", nullable = false)
    private Double cantidad;

    @Enumerated(EnumType.STRING)
    @Column(name = "unidad_medida", nullable = false, length = 10)
    private UnidadMedida unidadMedida;

}