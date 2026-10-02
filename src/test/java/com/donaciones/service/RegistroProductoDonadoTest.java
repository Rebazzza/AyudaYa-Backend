package com.donaciones.service;

import com.donaciones.dto.request.DetalleDonacionRequestDTO;
import com.donaciones.dto.request.DonacionRegistroRequestDTO;
import com.donaciones.dto.response.DetalleDonacionResponse;
import com.donaciones.dto.response.DonacionResponse;
import com.donaciones.dto.response.DonacionResponseDTO;
import com.donaciones.entity.CategoriaInsumo;
import com.donaciones.entity.DetalleDonacion;
import com.donaciones.entity.Donacion;
import com.donaciones.entity.HistorialEstado;
import com.donaciones.entity.LocalRecepcion;
import com.donaciones.entity.Usuario;
import com.donaciones.exception.BadRequestException;
import com.donaciones.exception.ResourceNotFoundException;
import com.donaciones.repository.CategoriaInsumoRepository;
import com.donaciones.repository.DonacionRepository;
import com.donaciones.repository.HistorialEstadoRepository;
import com.donaciones.repository.LocalRecepcionRepository;
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
@DisplayName("Registro de producto donado por el personal de la organización")
class RegistroProductoDonadoTest {

    private static final Long ID_USUARIO = 7L;
    private static final Long ID_LOCAL = 2L;
    private static final Integer ID_DONACION = 3;
    private static final Integer ID_DETALLE = 31;
    private static final Integer ID_CATEGORIA_ALIMENTOS = 1;
    private static final String DESCRIPCION = "Arroz extra 1 kg";

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

    // CA1: el producto se registra con sus variables criticas y aparece en el listado.
    @Test
    @DisplayName("CA1: Registra 'Arroz extra 1 kg' de categoría Alimentos con cantidad 50 y no perecible")
    void registrarDonacion_conProductoNoPerecible_guardaSusVariablesCriticas() {
        prepararRegistro();

        DonacionResponseDTO response = donacionService.registrarDonacion(
                solicitudProductoArroz(new BigDecimal("50")));

        assertThat(response.getEstadoActual()).isEqualTo("REGISTRADO");
        assertThat(response.getDetalles()).hasSize(1);
        DetalleDonacionResponse producto = response.getDetalles().getFirst();
        assertThat(producto.getDescripcionDetalle()).isEqualTo(DESCRIPCION);
        assertThat(producto.getNombreCategoria()).isEqualTo("Alimentos");
        assertThat(producto.getCantidadDeclarada()).isEqualByComparingTo("50");
        assertThat(producto.getFechaVencimiento()).isNull();

        ArgumentCaptor<Donacion> captor = ArgumentCaptor.forClass(Donacion.class);
        verify(donacionRepository).save(captor.capture());
        DetalleDonacion persistido = captor.getValue().getDetalles().getFirst();
        assertThat(persistido.getDescripcionDetalle()).isEqualTo(DESCRIPCION);
        assertThat(persistido.getCantidadDeclarada()).isEqualByComparingTo("50");
        assertThat(persistido.getCategoria().getNombreCategoria()).isEqualTo("Alimentos");
        assertThat(persistido.getCategoria().getRefrigerar()).isFalse();
        assertThat(persistido.getActivo()).isTrue();
    }

    @Test
    @DisplayName("CA1: El producto registrado aparece en el listado de donaciones del usuario")
    void registrarDonacion_conProductoValido_apareceEnElListadoDelUsuario() {
        prepararRegistro();

        donacionService.registrarDonacion(solicitudProductoArroz(new BigDecimal("50")));

        ArgumentCaptor<Donacion> captor = ArgumentCaptor.forClass(Donacion.class);
        verify(donacionRepository).save(captor.capture());
        Donacion guardada = captor.getValue();
        when(donacionRepository.findByUsuarioIdUsuarioOrderByFechaRegistroDesc(ID_USUARIO))
                .thenReturn(List.of(guardada));

        List<DonacionResponseDTO> listado = donacionService.obtenerDonacionesPorUsuario(ID_USUARIO);

        assertThat(listado).hasSize(1);
        assertThat(listado.getFirst().getDetalles())
                .extracting(DetalleDonacionResponse::getDescripcionDetalle)
                .containsExactly(DESCRIPCION);
        assertThat(listado.getFirst().getDetalles())
                .extracting(DetalleDonacionResponse::getCantidadDeclarada)
                .containsExactly(new BigDecimal("50"));
    }

    // CA3: la eliminacion es logica, el registro no se borra.
    @Test
    @DisplayName("CA3: La baja marca el producto como inactivo y conserva el registro en la donación")
    void darDeBajaProducto_marcaInactivoYConservaElRegistro() {
        Donacion donacion = donacionConProductoActivo();
        when(donacionRepository.findById(ID_DONACION)).thenReturn(Optional.of(donacion));
        when(donacionRepository.save(any(Donacion.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        DetalleDonacionResponse baja = donacionService.darDeBajaProducto(ID_DONACION, ID_DETALLE);

        assertThat(baja.getActivo()).isFalse();
        DetalleDonacion detalle = donacion.getDetalles().getFirst();
        assertThat(detalle.getIdDetalle()).isEqualTo(ID_DETALLE);
        assertThat(detalle.getDescripcionDetalle()).isEqualTo(DESCRIPCION);
        assertThat(detalle.getActivo()).isFalse();
        verify(donacionRepository, never()).delete(any(Donacion.class));
        verify(donacionRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("CA3: Tras la baja, el producto deja de mostrarse activo en el listado")
    void darDeBajaProducto_dejaDeMostrarseEnElListado() {
        Donacion donacion = donacionConProductoActivo();
        when(donacionRepository.findById(ID_DONACION)).thenReturn(Optional.of(donacion));
        when(donacionRepository.save(any(Donacion.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        donacionService.darDeBajaProducto(ID_DONACION, ID_DETALLE);
        DonacionResponse response = donacionService.getById(ID_DONACION);

        assertThat(response.getDetalles()).isEmpty();
        assertThat(donacion.getDetalles()).isNotEmpty();
    }

    @Test
    @DisplayName("CA3: Dar de baja dos veces el mismo producto se rechaza sin romper la integridad")
    void darDeBajaProducto_yaInactivo_lanzaBadRequestException() {
        Donacion donacion = donacionConProductoActivo();
        donacion.getDetalles().getFirst().setActivo(false);
        when(donacionRepository.findById(ID_DONACION)).thenReturn(Optional.of(donacion));

        assertThatThrownBy(() -> donacionService.darDeBajaProducto(ID_DONACION, ID_DETALLE))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("El producto con id 31 ya se encuentra dado de baja");

        verify(donacionRepository, never()).save(any(Donacion.class));
    }

    @Test
    @DisplayName("CA3: Dar de baja un producto inexistente responde recurso no encontrado")
    void darDeBajaProducto_inexistente_lanzaResourceNotFoundException() {
        Donacion donacion = donacionConProductoActivo();
        when(donacionRepository.findById(ID_DONACION)).thenReturn(Optional.of(donacion));

        assertThatThrownBy(() -> donacionService.darDeBajaProducto(ID_DONACION, 999))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("No se encontró el producto con id 999 en la donación 3");

        verify(donacionRepository, never()).save(any(Donacion.class));
    }

    private void prepararRegistro() {
        when(localRepository.findById(ID_LOCAL)).thenReturn(Optional.of(localRecepcion()));
        when(usuarioRepository.findById(ID_USUARIO)).thenReturn(Optional.of(usuarioDonante()));
        when(categoriaRepository.findById(ID_CATEGORIA_ALIMENTOS)).thenReturn(Optional.of(categoriaAlimentos()));
        when(donacionRepository.existsByCodigoSeguimiento(any())).thenReturn(false);
        when(donacionRepository.save(any(Donacion.class))).thenAnswer(invocacion -> {
            Donacion donacion = invocacion.getArgument(0);
            donacion.setIdDonacion(ID_DONACION);
            donacion.prePersist();
            return donacion;
        });
    }

    private DonacionRegistroRequestDTO solicitudProductoArroz(BigDecimal cantidad) {
        return DonacionRegistroRequestDTO.builder()
                .idUsuario(ID_USUARIO)
                .idLocalRecepcion(ID_LOCAL)
                .detalles(List.of(DetalleDonacionRequestDTO.builder()
                        .idCategoria(ID_CATEGORIA_ALIMENTOS)
                        .descripcionDetalle(DESCRIPCION)
                        .cantidadDeclarada(cantidad)
                        .fechaVencimiento(null)
                        .build()))
                .build();
    }

    private Donacion donacionConProductoActivo() {
        Donacion donacion = Donacion.builder()
                .idDonacion(ID_DONACION)
                .codigoSeguimiento("DON-2026-A1B2C3")
                .estadoActual("REGISTRADO")
                .usuario(usuarioDonante())
                .localRecepcion(localRecepcion())
                .build();
        donacion.addDetalle(DetalleDonacion.builder()
                .idDetalle(ID_DETALLE)
                .categoria(categoriaAlimentos())
                .descripcionDetalle(DESCRIPCION)
                .cantidadDeclarada(new BigDecimal("50"))
                .activo(true)
                .build());
        return donacion;
    }

    private CategoriaInsumo categoriaAlimentos() {
        return CategoriaInsumo.builder()
                .idCategoria(ID_CATEGORIA_ALIMENTOS)
                .nombreCategoria("Alimentos")
                .unidadMedCate("kg")
                .refrigerar(false)
                .build();
    }

    private Usuario usuarioDonante() {
        return Usuario.builder()
                .idUsuario(ID_USUARIO)
                .nombreUsuario("María")
                .apellidosUsuario("García López")
                .dniUsuario("87654321")
                .build();
    }

    private LocalRecepcion localRecepcion() {
        return LocalRecepcion.builder()
                .idLocal(ID_LOCAL)
                .nombreLocal("Local Central")
                .direccionLocal("Av. Ejemplo 123")
                .build();
    }

}