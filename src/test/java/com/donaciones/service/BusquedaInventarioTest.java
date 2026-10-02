package com.donaciones.service;

import com.donaciones.dto.response.PaginaDTO;
import com.donaciones.dto.response.ProductoInventarioDTO;
import com.donaciones.entity.CategoriaInsumo;
import com.donaciones.entity.DetalleDonacion;
import com.donaciones.entity.Donacion;
import com.donaciones.entity.LocalRecepcion;
import com.donaciones.exception.BadRequestException;
import com.donaciones.repository.DetalleDonacionRepository;
import com.donaciones.repository.LocalRecepcionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
@DisplayName("Búsqueda y filtrado del inventario en almacén")
class BusquedaInventarioTest {

    private static final Long ID_LOCAL = 2L;

    @Mock
    private LocalRecepcionRepository localRepository;

    @Mock
    private DetalleDonacionRepository detalleRepository;

    @InjectMocks
    private AlmacenServiceImpl almacenService;

    // HU-03: Búsqueda por texto en el inventario del almacén.
    @Test
    @DisplayName("Lista los productos cuya categoría coincide con el texto buscado, sin distinguir mayúsculas")
    void buscarProductosInventario_conTextoDelNombre_retornaProductosCuyaCategoriaCoincide() {
        prepararInventario(inventario());

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
        prepararInventario(inventario());

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
        prepararInventario(inventario());

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
        prepararInventario(inventario());

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
        List<DetalleDonacion> inventario = inventario();
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

    private void prepararInventario(List<DetalleDonacion> inventario) {
        when(localRepository.findById(ID_LOCAL)).thenReturn(Optional.of(localRecepcion()));
        when(detalleRepository.findByDonacionEstadoActualAndDonacionLocalRecepcionIdLocal("EN_ALMACEN", ID_LOCAL))
                .thenReturn(inventario);
    }

    private List<DetalleDonacion> inventario() {
        LocalDateTime ahora = LocalDateTime.now();
        return List.of(
                detalle(31, 1, "Alimentos", "Arroz extra 1 kg", ahora.plusDays(60)),
                detalle(32, 1, "Alimentos", "Azúcar rubia 1 kg", ahora.plusDays(5)),
                detalle(33, 1, "Alimentos", "Frijol canario 500 g", ahora.minusDays(2)),
                detalle(34, 2, "Medicinas", "Paracetamol 500 mg", ahora.plusDays(5)),
                detalle(35, 3, "Abrigo", "Frazada de lana", null));
    }

    private DetalleDonacion detalle(
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

    private LocalRecepcion localRecepcion() {
        return LocalRecepcion.builder()
                .idLocal(ID_LOCAL)
                .nombreLocal("Local Central")
                .direccionLocal("Av. Ejemplo 123")
                .build();
    }

}
