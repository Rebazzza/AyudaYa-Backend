package com.donaciones.service;

import com.donaciones.dto.request.CorroboracionRequestDTO;
import com.donaciones.dto.request.ItemCorroboracionRequestDTO;
import com.donaciones.dto.response.DetalleDonacionResponse;
import com.donaciones.dto.response.DonacionResponse;
import com.donaciones.entity.CategoriaInsumo;
import com.donaciones.entity.DetalleDonacion;
import com.donaciones.entity.Donacion;
import com.donaciones.entity.HistorialEstado;
import com.donaciones.entity.LocalRecepcion;
import com.donaciones.entity.Trabajador;
import com.donaciones.entity.Usuario;
import com.donaciones.exception.BadRequestException;
import com.donaciones.repository.DetalleDonacionRepository;
import com.donaciones.repository.DonacionRepository;
import com.donaciones.repository.HistorialEstadoRepository;
import com.donaciones.repository.LocalRecepcionRepository;
import com.donaciones.repository.NotificacionRepository;
import com.donaciones.repository.TrabajadorRepository;
import com.donaciones.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Corroboración de recepción de donaciones en almacén")
class AlmacenServiceTest {

    private static final Integer ID_DONACION = 3;
    private static final Integer ID_TRABAJADOR = 4;
    private static final Integer ID_DETALLE = 31;
    private static final Long ID_LOCAL = 2L;

    @Mock
    private DonacionRepository donacionRepository;

    @Mock
    private LocalRecepcionRepository localRepository;

    @Mock
    private TrabajadorRepository trabajadorRepository;

    @Mock
    private DetalleDonacionRepository detalleRepository;

    @Mock
    private HistorialEstadoRepository historialRepository;

    @Mock
    private NotificacionRepository notificacionRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private DonacionService donacionService;

    @InjectMocks
    private AlmacenServiceImpl almacenService;

    @Test
    @DisplayName("Guarda la cantidad realmente recibida y clasifica el faltante")
    void corroborarRecepcion_conCantidadRecibidaMenorActualizaDetalleYEstado() {
        Donacion donacion = donacionConDetalle();
        LocalRecepcion local = localRecepcion();
        Trabajador trabajador = trabajador();
        DonacionResponse respuestaEsperada = respuestaDonacion();
        when(donacionRepository.findById(ID_DONACION)).thenReturn(Optional.of(donacion));
        when(localRepository.findById(ID_LOCAL)).thenReturn(Optional.of(local));
        when(trabajadorRepository.findById(ID_TRABAJADOR)).thenReturn(Optional.of(trabajador));
        when(donacionService.getById(ID_DONACION)).thenReturn(respuestaEsperada);
        when(usuarioRepository.findByRolNombreRol("Administrador")).thenReturn(List.of());

        DonacionResponse response = almacenService.corroborarRecepcion(solicitud("8"));

        assertThat(response).isSameAs(respuestaEsperada);
        assertThat(donacion.getEstadoActual()).isEqualTo("EN_ALMACEN");
        assertThat(donacion.getLocalRecepcion()).isSameAs(local);
        DetalleDonacion detalle = donacion.getDetalles().getFirst();
        assertThat(detalle.getCantidadDeclarada()).isEqualByComparingTo("10");
        assertThat(detalle.getCantidadVerificada()).isEqualByComparingTo("8");
        assertThat(detalle.getObservacionDetalle()).isEqualTo("Faltante");

        verify(donacionRepository).save(donacion);
        ArgumentCaptor<HistorialEstado> historialCaptor = ArgumentCaptor.forClass(HistorialEstado.class);
        verify(historialRepository).save(historialCaptor.capture());
        assertThat(historialCaptor.getValue().getDonacion()).isSameAs(donacion);
        assertThat(historialCaptor.getValue().getLocalRecepcion()).isSameAs(local);
        assertThat(historialCaptor.getValue().getTrabajador()).isSameAs(trabajador);
        assertThat(historialCaptor.getValue().getEstado()).isEqualTo("EN_ALMACEN");
        verify(donacionService).getById(ID_DONACION);
    }

    @Test
    @DisplayName("No guarda la corroboración si no se verifican todos los productos")
    void corroborarRecepcion_conProductoSinVerificar_lanzaBadRequestException() {
        Donacion donacion = donacionConDetalle();
        when(donacionRepository.findById(ID_DONACION)).thenReturn(Optional.of(donacion));
        when(localRepository.findById(ID_LOCAL)).thenReturn(Optional.of(localRecepcion()));
        when(trabajadorRepository.findById(ID_TRABAJADOR)).thenReturn(Optional.of(trabajador()));

        CorroboracionRequestDTO solicitud = CorroboracionRequestDTO.builder()
                .idDonacion(ID_DONACION)
                .idTrabajador(ID_TRABAJADOR)
                .idLocal(ID_LOCAL)
                .detalles(List.of())
                .build();

        assertThatThrownBy(() -> almacenService.corroborarRecepcion(solicitud))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Todos los insumos deben ser verificados");

        verify(donacionRepository, never()).save(any(Donacion.class));
        verify(historialRepository, never()).save(any(HistorialEstado.class));
        verify(donacionService, never()).getById(ID_DONACION);
    }

    private CorroboracionRequestDTO solicitud(String cantidadVerificada) {
        return CorroboracionRequestDTO.builder()
                .idDonacion(ID_DONACION)
                .idTrabajador(ID_TRABAJADOR)
                .idLocal(ID_LOCAL)
                .latitud(-12.046374)
                .longitud(-77.042793)
                .detalles(List.of(ItemCorroboracionRequestDTO.builder()
                        .idDetalle(ID_DETALLE)
                        .cantidadVerificada(new BigDecimal(cantidadVerificada))
                        .build()))
                .build();
    }

    private Donacion donacionConDetalle() {
        Donacion donacion = Donacion.builder()
                .idDonacion(ID_DONACION)
                .codigoSeguimiento("DON-2026-A1B2C3")
                .estadoActual("REGISTRADO")
                .usuario(Usuario.builder()
                        .idUsuario(8L)
                        .nombreUsuario("Carlos")
                        .apellidosUsuario("Pérez")
                        .build())
                .localRecepcion(localRecepcion())
                .build();
        donacion.addDetalle(DetalleDonacion.builder()
                .idDetalle(ID_DETALLE)
                .categoria(CategoriaInsumo.builder()
                        .idCategoria(1)
                        .nombreCategoria("Arroz")
                        .unidadMedCate("kg")
                        .refrigerar(false)
                        .build())
                .cantidadDeclarada(new BigDecimal("10"))
                .build());
        return donacion;
    }

    private LocalRecepcion localRecepcion() {
        return LocalRecepcion.builder()
                .idLocal(ID_LOCAL)
                .nombreLocal("Local Central")
                .direccionLocal("Av. Ejemplo 123")
                .build();
    }

    private Trabajador trabajador() {
        return Trabajador.builder()
                .idTrabajador(ID_TRABAJADOR)
                .usuario(Usuario.builder().idUsuario(9L).build())
                .localRecepcion(localRecepcion())
                .build();
    }

    private DonacionResponse respuestaDonacion() {
        return DonacionResponse.builder()
                .idDonacion(ID_DONACION)
                .codigoSeguimiento("DON-2026-A1B2C3")
                .estadoActual("EN_ALMACEN")
                .detalles(List.of(DetalleDonacionResponse.builder()
                        .idDetalle(ID_DETALLE)
                        .nombreCategoria("Arroz")
                        .cantidadDeclarada(new BigDecimal("10"))
                        .cantidadVerificada(new BigDecimal("8"))
                        .observacionDetalle("Faltante")
                        .build()))
                .build();
    }
}
