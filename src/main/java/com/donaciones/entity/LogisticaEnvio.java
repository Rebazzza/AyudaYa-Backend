package com.donaciones.entity;

import com.donaciones.enums.EstadoEnvio;
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

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
@Table(name = "logistica_envios")
public class LogisticaEnvio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_logistica_envio")
    private Long idLogisticaEnvio;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_donacion", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Donacion donacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_voluntario", nullable = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Usuario voluntario;

    @Column(name = "transportista", length = 100)
    private String transportista;

    @Column(name = "fecha_salida")
    private LocalDateTime fechaSalida;

    @Column(name = "fecha_llegada_estimada")
    private LocalDateTime fechaLlegadaEstimada;

    @Column(name = "fecha_llegada_real")
    private LocalDateTime fechaLlegadaReal;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_envio", nullable = false, length = 20)
    private EstadoEnvio estado;

    @Column(name = "observaciones", length = 255)
    private String observaciones;

}