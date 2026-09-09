package com.donaciones.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "LocalRecepcion")
public class LocalRecepcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_Local")
    private Long idLocal;

    @Column(name = "nombreLocal", nullable = false, length = 100)
    private String nombreLocal;

    @Column(name = "direccionLocal", nullable = false, length = 255)
    private String direccionLocal;

    @Column(name = "latitud", nullable = false, precision = 10, scale = 8)
    private BigDecimal latitud;

    @Column(name = "longitud", nullable = false, precision = 11, scale = 8)
    private BigDecimal longitud;

    @Column(name = "capacidadLocal_m3", nullable = false)
    private Double capacidadLocalM3;

    @Column(name = "telefonoLocal", length = 20)
    private String telefonoLocal;

    @Column(name = "estadoActivo", nullable = false)
    private Boolean estadoActivo;

}