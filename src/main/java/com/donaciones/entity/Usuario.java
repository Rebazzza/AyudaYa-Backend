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
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Usuario", uniqueConstraints = {
        @UniqueConstraint(name = "uk_usuario_dni", columnNames = "dniUsuario"),
        @UniqueConstraint(name = "uk_usuario_correo", columnNames = "correoUsuario")
})
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_Usuario")
    private Long idUsuario;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_Rol", nullable = false)
    private Rol rol;

    @Column(name = "dniUsuario", nullable = false, unique = true, length = 8)
    private String dniUsuario;

    @Column(name = "nombreUsuario", nullable = false, length = 50)
    private String nombreUsuario;

    @Column(name = "apellidosUsuario", nullable = false, length = 100)
    private String apellidosUsuario;

    @Column(name = "correoUsuario", nullable = false, unique = true, length = 100)
    private String correoUsuario;

    @Column(name = "contraseña", nullable = false, length = 100)
    private String contraseña;

    @Column(name = "telefonoUsuario", length = 15)
    private String telefonoUsuario;

    @CreationTimestamp
    @Column(name = "fechaRegistro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

}