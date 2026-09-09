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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "detalledonacion")
public class DetalleDonacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_Detalle")
    private Integer idDetalle;

    @Column(name = "descripcionDetalle", length = 200)
    private String descripcionDetalle;

    @Column(name = "cantidadDeclarada", nullable = false, precision = 10, scale = 2)
    private BigDecimal cantidadDeclarada;

    @Column(name = "cantidadVerificada", precision = 10, scale = 2)
    private BigDecimal cantidadVerificada;

    @Column(name = "fechaVencimiento")
    private LocalDateTime fechaVencimiento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_Donacion", nullable = false)
    private Donacion donacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_Categoria", nullable = false)
    private CategoriaInsumo categoria;

}