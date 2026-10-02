package com.donaciones.service;

import com.donaciones.dto.request.DetalleDonacionRequestDTO;
import com.donaciones.dto.request.DonacionRegistroRequestDTO;
import com.donaciones.dto.response.DonacionResponseDTO;
import com.donaciones.entity.CategoriaInsumo;
import com.donaciones.entity.LocalRecepcion;
import com.donaciones.entity.Usuario;
import com.donaciones.exception.BadRequestException;
import com.donaciones.repository.CategoriaInsumoRepository;
import com.donaciones.repository.DonacionRepository;
import com.donaciones.repository.HistorialEstadoRepository;
import com.donaciones.repository.LocalRecepcionRepository;
import com.donaciones.repository.TrabajadorRepository;
import com.donaciones.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * HU-15: Control de capacidad de locales (Juan Diego) — bloqueo al registrar una donación.
 * Casos de prueba cubiertos: CP-HU15.
 *
 * La ocupación del local se estima contando sus donaciones en estado EN_ALMACEN (ver nota de
 * supuesto en LocalServiceCapacidadTest), ya que el esquema no registra volumen en m3 por donación.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Bloqueo de registro de donaciones por capacidad del local")
class DonacionServiceCapacidadTest {

    @Mock
    private DonacionRepository donacionRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private LocalRecepcionRepository localRepository;
    @Mock
    private TrabajadorRepository trabajadorRepository;
    @Mock
    private CategoriaInsumoRepository categoriaRepository;
    @Mock
    private HistorialEstadoRepository historialRepository;
    @Mock
    private EmailService emailService;

    @InjectMocks
    private DonacionServiceImpl donacionService;

    private static final Long ID_USUARIO = 1L;
    private static final Long ID_LOCAL = 1L;

    @Test
    @DisplayName("Rechaza el registro cuando el local elegido ya alcanzó su capacidad declarada")
    void registrarDonacion_conLocalLleno_lanzaBadRequestExceptionYNoGuardaLaDonacion() {
        LocalRecepcion local = localConCapacidad(5.0);
        when(localRepository.findById(ID_LOCAL)).thenReturn(Optional.of(local));
        when(donacionRepository.countByLocalRecepcionIdLocalAndEstadoActual(ID_LOCAL, "EN_ALMACEN")).thenReturn(5L);

        DonacionRegistroRequestDTO dto = DonacionRegistroRequestDTO.builder()
                .idUsuario(ID_USUARIO)
                .idLocalRecepcion(ID_LOCAL)
                .detalles(List.of(detalleRequest()))
                .build();

        assertThatThrownBy(() -> donacionService.registrarDonacion(dto))
                .isInstanceOf(BadRequestException.class);

        verify(donacionRepository, never()).save(any());
        verify(usuarioRepository, never()).findById(any());
    }

    @Test
    @DisplayName("Permite el registro cuando el local todavía tiene capacidad disponible")
    void registrarDonacion_conCapacidadDisponible_registraLaDonacionNormalmente() {
        LocalRecepcion local = localConCapacidad(5.0);
        Usuario usuario = Usuario.builder().idUsuario(ID_USUARIO).build();
        CategoriaInsumo categoria = CategoriaInsumo.builder()
                .idCategoria(1).nombreCategoria("Alimentos").unidadMedCate("kg").build();

        when(localRepository.findById(ID_LOCAL)).thenReturn(Optional.of(local));
        when(donacionRepository.countByLocalRecepcionIdLocalAndEstadoActual(ID_LOCAL, "EN_ALMACEN")).thenReturn(4L);
        when(usuarioRepository.findById(ID_USUARIO)).thenReturn(Optional.of(usuario));
        when(categoriaRepository.findById(1)).thenReturn(Optional.of(categoria));
        when(donacionRepository.existsByCodigoSeguimiento(anyString())).thenReturn(false);

        DonacionRegistroRequestDTO dto = DonacionRegistroRequestDTO.builder()
                .idUsuario(ID_USUARIO)
                .idLocalRecepcion(ID_LOCAL)
                .detalles(List.of(detalleRequest()))
                .build();

        DonacionResponseDTO response = donacionService.registrarDonacion(dto);

        assertThat(response.getEstadoActual()).isEqualTo("REGISTRADO");
        verify(donacionRepository).save(any());
    }

    private DetalleDonacionRequestDTO detalleRequest() {
        return DetalleDonacionRequestDTO.builder()
                .idCategoria(1)
                .descripcionDetalle("Arroz")
                .cantidadDeclarada(new BigDecimal("10"))
                .build();
    }

    private LocalRecepcion localConCapacidad(Double capacidadLocalM3) {
        return LocalRecepcion.builder()
                .idLocal(ID_LOCAL)
                .nombreLocal("Coliseo Manuel Bonilla")
                .capacidadLocalM3(capacidadLocalM3)
                .estadoActivo(true)
                .build();
    }

}
