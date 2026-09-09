package com.donaciones.entity;

import com.donaciones.enums.EstadoSolicitud;
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
@Table(name = "beneficiario_solicitudes")
public class BeneficiarioSolicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_beneficiario_solicitud")
    private Long idBeneficiarioSolicitud;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario_receptor", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Usuario receptor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_local_gestor", nullable = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private LocalRecepcion localGestor;

    @Column(name = "descripcion_necesidad", nullable = false, length = 500)
    private String descripcionNecesidad;

    @Column(name = "fecha_solicitud", nullable = false, updatable = false)
    private LocalDateTime fechaSolicitud;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoSolicitud estado;

    @Builder.Default
    @OneToMany(mappedBy = "solicitud", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<EntregaDonacion> entregas = new ArrayList<>();

    public void addEntrega(EntregaDonacion entrega) {
        entregas.add(entrega);
        entrega.setSolicitud(this);
    }

    public void removeEntrega(EntregaDonacion entrega) {
        entregas.remove(entrega);
        entrega.setSolicitud(null);
    }

}