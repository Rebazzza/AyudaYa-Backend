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
@Table(name = "entregas_donacion")
public class EntregaDonacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_entrega_donacion")
    private Long idEntregaDonacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario_receptor", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Usuario receptor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_beneficiario_solicitud", nullable = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private BeneficiarioSolicitud solicitud;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_local_origen", nullable = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private LocalRecepcion localOrigen;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_tipo_categoria_item", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private TipoCategoriaItem tipoCategoriaItem;

    @Column(name = "cantidad_entregada", nullable = false)
    private Double cantidadEntregada;

    @Column(name = "fecha_entrega", nullable = false)
    private LocalDateTime fechaEntrega;

    @Column(name = "confirmacion_receptor", nullable = false)
    private Boolean confirmacionReceptor;

    @Column(name = "observaciones", length = 255)
    private String observaciones;

}