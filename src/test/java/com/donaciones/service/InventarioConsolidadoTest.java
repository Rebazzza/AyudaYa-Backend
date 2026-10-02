package com.donaciones.service;

import com.donaciones.dto.response.ResumenInventarioDTO;
import com.donaciones.entity.CategoriaInsumo;
import com.donaciones.entity.DetalleDonacion;
import com.donaciones.repository.DetalleDonacionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Total consolidado de inventario por categoría")
class InventarioConsolidadoTest {

    private static final Long ID_LOCAL = 2L;

    @Mock
    private DetalleDonacionRepository detalleRepository;

    @InjectMocks
    private AlmacenServiceImpl almacenService;

    @Test
    @DisplayName("CA3: Consolida el stock verificado sumando los productos de cada categoría")
    void obtenerInventario_agrupaYSumaPorCategoria() {
        CategoriaInsumo alimentos = categoria(1, "Alimentos", "kg", true);
        CategoriaInsumo abrigo = categoria(5, "Abrigo", "unidades", true);
        when(detalleRepository
                .findByDonacionEstadoActualAndDonacionLocalRecepcionIdLocalAndActivoTrue("EN_ALMACEN", ID_LOCAL))
                .thenReturn(List.of(
                        detalle(alimentos, "10"),
                        detalle(alimentos, "5.5"),
                        detalle(abrigo, "3")));

        List<ResumenInventarioDTO> inventario = almacenService.obtenerInventarioPorLocal(ID_LOCAL);

        assertThat(inventario).hasSize(2);
        assertThat(inventario)
                .extracting(ResumenInventarioDTO::getNombreCategoria)
                .containsExactly("Abrigo", "Alimentos");
        assertThat(inventario)
                .filteredOn(resumen -> resumen.getNombreCategoria().equals("Alimentos"))
                .singleElement()
                .satisfies(resumen -> assertThat(resumen.getStockTotalVerificado())
                        .isEqualByComparingTo("15.5"));
        assertThat(inventario)
                .filteredOn(resumen -> resumen.getNombreCategoria().equals("Abrigo"))
                .singleElement()
                .satisfies(resumen -> assertThat(resumen.getStockTotalVerificado())
                        .isEqualByComparingTo("3"));
    }

    @Test
    @DisplayName("CA3: El total por categoría coincide con la suma de los productos registrados")
    void obtenerInventario_totalPorCategoriaCoincideConLosProductos() {
        CategoriaInsumo alimentos = categoria(1, "Alimentos", "kg", true);
        when(detalleRepository
                .findByDonacionEstadoActualAndDonacionLocalRecepcionIdLocalAndActivoTrue("EN_ALMACEN", ID_LOCAL))
                .thenReturn(List.of(
                        detalle(alimentos, "2.25"),
                        detalle(alimentos, "0.75"),
                        detalle(alimentos, "7")));

        ResumenInventarioDTO alimentosResumen = almacenService.obtenerInventarioPorLocal(ID_LOCAL).getFirst();

        assertThat(alimentosResumen.getStockTotalVerificado()).isEqualByComparingTo("10");
        assertThat(alimentosResumen.getUnidadMedida()).isEqualTo("kg");
        assertThat(alimentosResumen.getTotalItemsIncidencia()).isZero();
    }

    @Test
    @DisplayName("CA3: Ignora las categorías deshabilitadas en el consolidado")
    void obtenerInventario_excluyeCategoriasInactivas() {
        CategoriaInsumo alimentos = categoria(1, "Alimentos", "kg", true);
        CategoriaInsumo ropaDeshabilitada = categoria(9, "Ropa", "unidades", false);
        when(detalleRepository
                .findByDonacionEstadoActualAndDonacionLocalRecepcionIdLocalAndActivoTrue("EN_ALMACEN", ID_LOCAL))
                .thenReturn(List.of(
                        detalle(alimentos, "10"),
                        detalle(ropaDeshabilitada, "100")));

        List<ResumenInventarioDTO> inventario = almacenService.obtenerInventarioPorLocal(ID_LOCAL);

        assertThat(inventario)
                .extracting(ResumenInventarioDTO::getNombreCategoria)
                .containsExactly("Alimentos")
                .doesNotContain("Ropa");
    }

    private DetalleDonacion detalle(CategoriaInsumo categoria, String cantidadVerificada) {
        return DetalleDonacion.builder()
                .categoria(categoria)
                .cantidadDeclarada(new BigDecimal(cantidadVerificada))
                .cantidadVerificada(new BigDecimal(cantidadVerificada))
                .activo(true)
                .build();
    }

    private CategoriaInsumo categoria(Integer id, String nombre, String unidad, boolean activo) {
        return CategoriaInsumo.builder()
                .idCategoria(id)
                .nombreCategoria(nombre)
                .unidadMedCate(unidad)
                .refrigerar(false)
                .activo(activo)
                .build();
    }

}