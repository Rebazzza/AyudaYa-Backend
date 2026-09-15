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

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "donacionmonetaria")
public class DonacionMonetaria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_DonacionMonetaria")
    private Integer idDonacionMonetaria;

    @Column(name = "monto", nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    @Column(name = "moneda", nullable = false, length = 10)
    private String moneda;

    @Column(name = "metodoPago", nullable = false, length = 30)
    private String metodoPago;

    @Column(name = "numeroOperacion", unique = true, length = 50)
    private String numeroOperacion;

    @Column(name = "comprobanteUrl", length = 500)
    private String comprobanteUrl;

    @Column(name = "estadoVerificacion", nullable = false, length = 20)
    private String estadoVerificacion;

    @Column(name = "fechaRegistro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_Usuario", nullable = false)
    private Usuario usuario;

    @PrePersist
    public void prePersist() {
        if (moneda == null) {
            moneda = "PEN";
        }
        if (estadoVerificacion == null) {
            estadoVerificacion = "PENDIENTE";
        }
        if (fechaRegistro == null) {
            fechaRegistro = LocalDateTime.now();
        }
    }

}