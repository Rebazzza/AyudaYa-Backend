package com.donaciones.service;

import com.donaciones.dto.request.CorroboracionRequestDTO;
import com.donaciones.dto.request.ItemCorroboracionRequestDTO;
import com.donaciones.dto.response.DetalleDonacionResponse;
import com.donaciones.dto.response.DonacionResponse;
import com.donaciones.dto.response.PaginaDTO;
import com.donaciones.dto.response.ProductoInventarioDTO;
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
import java.time.LocalDateTime;
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

    // HU-03: Búsqueda por texto en el inventario del almacén.
    @Test
    @DisplayName("Lista los productos cuya categoría coincide con el texto buscado, sin distinguir mayúsculas")
    void buscarProductosInventario_conTextoDelNombre_retornaProductosCuyaCategoriaCoincide() {
        prepararInventarioHU03(inventarioHU03());

        PaginaDTO<ProductoInventarioDTO> pagina = almacenService
                .buscarProductosInventario(ID_LOCAL, "ALIMENTOS", null, null, 0, 10);

        assertThat(pagina.getContenido())
                .extracting(ProductoInventarioDTO::getIdDetalle)
                .containsExactly(31, 32, 33);
        assertThat(pagina.getTotalElementos()).isEqualTo(3);
    }

    @Test
    @DisplayName("Lista los productos cuya descripción coincide con un fragmento, sin distinguir tildes")
    void buscarProductosInventario_conFragmentoDeLaDescripcion_retornaProductosCuyaDescripcionCoincide() {
        prepararInventarioHU03(inventarioHU03());

        PaginaDTO<ProductoInventarioDTO> pagina = almacenService
                .buscarProductosInventario(ID_LOCAL, "azucar rub", null, null, 0, 10);

        assertThat(pagina.getContenido()).hasSize(1);
        assertThat(pagina.getContenido().getFirst().getIdDetalle()).isEqualTo(32);
        assertThat(pagina.getContenido().getFirst().getDescripcionDetalle()).isEqualTo("Azúcar rubia 1 kg");
    }

    // HU-03: Filtros combinados por categoría y estado de conservación/vencimiento.
    @Test
    @DisplayName("Muestra solo los productos que cumplen a la vez la categoría y el estado de conservación")
    void buscarProductosInventario_conCategoriaYEstadoDeConservacion_retornaSoloProductosQueCumplenAmbos() {
        prepararInventarioHU03(inventarioHU03());

        PaginaDTO<ProductoInventarioDTO> pagina = almacenService
                .buscarProductosInventario(ID_LOCAL, null, 1, "POR_VENCER", 0, 10);

        assertThat(pagina.getContenido())
                .extracting(ProductoInventarioDTO::getIdDetalle)
                .containsExactly(32);
        assertThat(pagina.getContenido().getFirst().getEstadoConservacion()).isEqualTo("POR_VENCER");
        assertThat(pagina.getContenido().getFirst().getNombreCategoria()).isEqualTo("Alimentos");
    }

    @Test
    @DisplayName("Rechaza el estado de conservación inválido sin consultar el inventario")
    void buscarProductosInventario_conEstadoDeConservacionInvalido_lanzaBadRequestException() {
        when(localRepository.findById(ID_LOCAL)).thenReturn(Optional.of(localRecepcion()));

        assertThatThrownBy(() -> almacenService
                .buscarProductosInventario(ID_LOCAL, null, null, "CADUCADO", 0, 10))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Estado de conservación inválido: CADUCADO");

        verify(detalleRepository, never())
                .findByDonacionEstadoActualAndDonacionLocalRecepcionIdLocal(any(), any());
    }

    // HU-03: Resultados paginados y actualizados en tiempo real.
    @Test
    @DisplayName("Pagina los resultados e indica el total de elementos y de páginas")
    void buscarProductosInventario_conMasDeUnaPagina_retornaResultadosPaginados() {
        prepararInventarioHU03(inventarioHU03());

        PaginaDTO<ProductoInventarioDTO> primera = almacenService
                .buscarProductosInventario(ID_LOCAL, null, null, null, 0, 2);
        PaginaDTO<ProductoInventarioDTO> ultima = almacenService
                .buscarProductosInventario(ID_LOCAL, null, null, null, 2, 2);

        assertThat(primera.getContenido())
                .extracting(ProductoInventarioDTO::getIdDetalle)
                .containsExactly(35, 31);
        assertThat(primera.getTotalElementos()).isEqualTo(5);
        assertThat(primera.getTotalPaginas()).isEqualTo(3);
        assertThat(ultima.getContenido())
                .extracting(ProductoInventarioDTO::getIdDetalle)
                .containsExactly(34);
        assertThat(ultima.getPagina()).isEqualTo(2);
    }

    @Test
    @DisplayName("Refleja en la siguiente consulta el producto registrado después de la primera")
    void buscarProductosInventario_trasRegistrarUnProducto_laSiguienteConsultaLoIncluye() {
        List<DetalleDonacion> inventario = inventarioHU03();
        when(localRepository.findById(ID_LOCAL)).thenReturn(Optional.of(localRecepcion()));
        when(detalleRepository.findByDonacionEstadoActualAndDonacionLocalRecepcionIdLocal("EN_ALMACEN", ID_LOCAL))
                .thenReturn(List.of(inventario.get(0)), List.of(inventario.get(0), inventario.get(3)));

        PaginaDTO<ProductoInventarioDTO> antes = almacenService
                .buscarProductosInventario(ID_LOCAL, null, null, null, 0, 10);
        PaginaDTO<ProductoInventarioDTO> despues = almacenService
                .buscarProductosInventario(ID_LOCAL, null, null, null, 0, 10);

        assertThat(antes.getTotalElementos()).isEqualTo(1);
        assertThat(despues.getTotalElementos()).isEqualTo(2);
        assertThat(despues.getContenido())
                .extracting(ProductoInventarioDTO::getIdDetalle)
                .containsExactly(31, 34);
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

    private void prepararInventarioHU03(List<DetalleDonacion> inventario) {
        when(localRepository.findById(ID_LOCAL)).thenReturn(Optional.of(localRecepcion()));
        when(detalleRepository.findByDonacionEstadoActualAndDonacionLocalRecepcionIdLocal("EN_ALMACEN", ID_LOCAL))
                .thenReturn(inventario);
    }

    private List<DetalleDonacion> inventarioHU03() {
        LocalDateTime ahora = LocalDateTime.now();
        return List.of(
                detalleHU03(31, 1, "Alimentos", "Arroz extra 1 kg", ahora.plusDays(60)),
                detalleHU03(32, 1, "Alimentos", "Azúcar rubia 1 kg", ahora.plusDays(5)),
                detalleHU03(33, 1, "Alimentos", "Frijol canario 500 g", ahora.minusDays(2)),
                detalleHU03(34, 2, "Medicinas", "Paracetamol 500 mg", ahora.plusDays(5)),
                detalleHU03(35, 3, "Abrigo", "Frazada de lana", null));
    }

    private DetalleDonacion detalleHU03(
            Integer id, Integer idCategoria, String nombreCategoria, String descripcion, LocalDateTime vencimiento) {
        return DetalleDonacion.builder()
                .idDetalle(id)
                .donacion(Donacion.builder()
                        .idDonacion(id)
                        .codigoSeguimiento("DON-2026-0000" + id)
                        .estadoActual("EN_ALMACEN")
                        .build())
                .categoria(CategoriaInsumo.builder()
                        .idCategoria(idCategoria)
                        .nombreCategoria(nombreCategoria)
                        .unidadMedCate("kg")
                        .refrigerar(false)
                        .build())
                .descripcionDetalle(descripcion)
                .cantidadDeclarada(new BigDecimal("10"))
                .cantidadVerificada(new BigDecimal("10"))
                .fechaVencimiento(vencimiento)
                .build();
    }
}
