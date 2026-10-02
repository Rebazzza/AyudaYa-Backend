package com.donaciones.service;

import com.donaciones.dto.request.CategoriaInsumoRequest;
import com.donaciones.dto.response.CategoriaInsumoResponse;
import com.donaciones.entity.CategoriaInsumo;
import com.donaciones.exception.BadRequestException;
import com.donaciones.exception.DuplicateResourceException;
import com.donaciones.repository.CategoriaInsumoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Gestión y clasificación de categorías de inventario")
class CategoriaInsumoServiceTest {

    private static final Integer ID_ABRIGO = 5;
    private static final Integer ID_ROPA = 9;

    @Mock
    private CategoriaInsumoRepository categoriaRepository;

    @InjectMocks
    private CategoriaInsumoService categoriaService;

    // CA1: crear y que aparezca en el listado.
    @Test
    @DisplayName("CA1: Crea la categoría 'Abrigo' y aparece en el listado de categorías")
    void crearCategoria_abrigo_apareceEnElListado() {
        when(categoriaRepository.existsByNombreCategoria("Abrigo")).thenReturn(false);
        when(categoriaRepository.save(any(CategoriaInsumo.class))).thenAnswer(invocacion -> {
            CategoriaInsumo categoria = invocacion.getArgument(0);
            categoria.setIdCategoria(ID_ABRIGO);
            return categoria;
        });
        when(categoriaRepository.findAll()).thenReturn(List.of(
                CategoriaInsumo.builder()
                        .idCategoria(ID_ABRIGO)
                        .nombreCategoria("Abrigo")
                        .unidadMedCate("unidades")
                        .refrigerar(false)
                        .activo(true)
                        .build()));

        CategoriaInsumoResponse creada = categoriaService.create(
                request("Abrigo", "unidades", false));
        List<CategoriaInsumoResponse> listado = categoriaService.list();

        assertThat(creada.getIdCategoria()).isEqualTo(ID_ABRIGO);
        assertThat(creada.getNombreCategoria()).isEqualTo("Abrigo");
        assertThat(creada.getActivo()).isTrue();
        assertThat(listado)
                .extracting(CategoriaInsumoResponse::getNombreCategoria)
                .contains("Abrigo");
    }

    // CA1: editar y que el cambio se refleje.
    @Test
    @DisplayName("CA1: Edita 'Abrigo' a 'Abrigo y calzado' y el cambio se refleja en el listado")
    void editarCategoria_guardaElNuevoNombreYSeRefleja() {
        CategoriaInsumo existente = categoria(ID_ABRIGO, "Abrigo", true);
        when(categoriaRepository.findById(ID_ABRIGO)).thenReturn(Optional.of(existente));
        when(categoriaRepository.existsByNombreCategoriaAndIdCategoriaNot("Abrigo y calzado", ID_ABRIGO))
                .thenReturn(false);
        when(categoriaRepository.save(any(CategoriaInsumo.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));
        when(categoriaRepository.findAll()).thenReturn(List.of(existente));

        CategoriaInsumoResponse editada = categoriaService.update(
                ID_ABRIGO, request("Abrigo y calzado", "unidades", false));
        List<CategoriaInsumoResponse> listado = categoriaService.list();

        assertThat(editada.getNombreCategoria()).isEqualTo("Abrigo y calzado");
        assertThat(listado)
                .extracting(CategoriaInsumoResponse::getNombreCategoria)
                .containsExactly("Abrigo y calzado");
    }

    @Test
    @DisplayName("CA1: Editar no permite dejar un nombre que ya usa otra categoría")
    void editarCategoria_conNombreDuplicado_lanzaDuplicateResourceException() {
        when(categoriaRepository.findById(ID_ABRIGO)).thenReturn(Optional.of(categoria(ID_ABRIGO, "Abrigo", true)));
        when(categoriaRepository.existsByNombreCategoriaAndIdCategoriaNot("Ropa", ID_ABRIGO)).thenReturn(true);

        assertThatThrownBy(() -> categoriaService.update(ID_ABRIGO, request("Ropa", "unidades", false)))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Ya existe una categoría con el nombre Ropa");

        verify(categoriaRepository, never()).save(any(CategoriaInsumo.class));
    }

    // CA1: deshabilitar deja la categoría inactiva, sin borrarla.
    @Test
    @DisplayName("CA1: Deshabilita 'Ropa' y queda con activo = false sin borrarse de la base de datos")
    void deshabilitarCategoria_ropa_quedaInactivaYNoSeBorra() {
        CategoriaInsumo ropa = categoria(ID_ROPA, "Ropa", true);
        when(categoriaRepository.findById(ID_ROPA)).thenReturn(Optional.of(ropa));
        when(categoriaRepository.save(any(CategoriaInsumo.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        CategoriaInsumoResponse deshabilitada = categoriaService.deshabilitar(ID_ROPA);

        assertThat(deshabilitada.getActivo()).isFalse();
        assertThat(ropa.getIdCategoria()).isEqualTo(ID_ROPA);
        assertThat(ropa.getNombreCategoria()).isEqualTo("Ropa");
        verify(categoriaRepository, never()).delete(any(CategoriaInsumo.class));
        verify(categoriaRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("CA1: Deshabilitar dos veces la misma categoría se rechaza")
    void deshabilitarCategoria_yaInactiva_lanzaBadRequestException() {
        when(categoriaRepository.findById(ID_ROPA)).thenReturn(Optional.of(categoria(ID_ROPA, "Ropa", false)));

        assertThatThrownBy(() -> categoriaService.deshabilitar(ID_ROPA))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("La categoría con id 9 ya se encuentra deshabilitada");

        verify(categoriaRepository, never()).save(any(CategoriaInsumo.class));
    }

    // CA2: el desplegable lista únicamente categorías activas.
    @Test
    @DisplayName("CA2: El listado de activas excluye la categoría deshabilitada 'Ropa'")
    void listarCategoriasActivas_excluyeLaDeshabilitada() {
        when(categoriaRepository.findByActivoTrue()).thenReturn(List.of(
                categoria(ID_ABRIGO, "Abrigo", true),
                categoria(3, "Alimentos", true)));

        List<CategoriaInsumoResponse> activas = categoriaService.listActive();

        assertThat(activas)
                .extracting(CategoriaInsumoResponse::getNombreCategoria)
                .containsExactly("Abrigo", "Alimentos")
                .doesNotContain("Ropa");
        assertThat(activas)
                .extracting(CategoriaInsumoResponse::getActivo)
                .containsOnly(true);
        verify(categoriaRepository).findByActivoTrue();
        verify(categoriaRepository, never()).findAll();
    }

    @Test
    @DisplayName("CA2: El listado de activas pide al repositorio solo las activas")
    void listarCategoriasActivas_consultaSoloElFinderDeActivas() {
        when(categoriaRepository.findByActivoTrue()).thenReturn(List.of());

        categoriaService.listActive();

        verify(categoriaRepository).findByActivoTrue();
        verify(categoriaRepository, never()).findAll();
    }

    private CategoriaInsumoRequest request(String nombre, String unidad, Boolean refrigerar) {
        return CategoriaInsumoRequest.builder()
                .nombreCategoria(nombre)
                .unidadMedCate(unidad)
                .refrigerar(refrigerar)
                .build();
    }

    private CategoriaInsumo categoria(Integer id, String nombre, boolean activo) {
        return CategoriaInsumo.builder()
                .idCategoria(id)
                .nombreCategoria(nombre)
                .unidadMedCate("unidades")
                .refrigerar(false)
                .activo(activo)
                .build();
    }

}