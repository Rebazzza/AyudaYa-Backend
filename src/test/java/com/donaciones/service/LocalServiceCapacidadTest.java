package com.donaciones.service;

import com.donaciones.dto.response.LocalCapacidadResponseDTO;
import com.donaciones.entity.LocalRecepcion;
import com.donaciones.repository.DonacionRepository;
import com.donaciones.repository.LocalRecepcionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * HU-15: Control de capacidad de locales (Juan Diego).
 * Casos de prueba cubiertos: CP-HU15.
 *
 * Nota: el esquema actual no registra un volumen en m3 por donación ni por categoría de insumo
 * (CategoriaInsumo solo guarda el nombre de la unidad de medida, no un factor de conversión a m3).
 * Por eso la ocupación del local se estima contando las donaciones en estado EN_ALMACEN
 * registradas en ese local, y se compara contra capacidadLocalM3 como un tope simplificado.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Capacidad de un local de recepción")
class LocalServiceCapacidadTest {

    @Mock
    private LocalRecepcionRepository localRepository;

    @Mock
    private DonacionRepository donacionRepository;

    @InjectMocks
    private LocalService localService;

    private static final Long ID_LOCAL = 1L;

    @Test
    @DisplayName("Con ocupación por debajo de la capacidad, el local no está lleno")
    void obtenerCapacidad_conOcupacionMenorALaCapacidad_localNoEstaLleno() {
        LocalRecepcion local = localConCapacidad(10.0);
        when(localRepository.findById(ID_LOCAL)).thenReturn(Optional.of(local));
        when(donacionRepository.countByLocalRecepcionIdLocalAndEstadoActual(ID_LOCAL, "EN_ALMACEN")).thenReturn(4L);

        LocalCapacidadResponseDTO capacidad = localService.obtenerCapacidad(ID_LOCAL);

        assertThat(capacidad.getCapacidadLocalM3()).isEqualTo(10.0);
        assertThat(capacidad.getDonacionesEnAlmacen()).isEqualTo(4L);
        assertThat(capacidad.getLleno()).isFalse();
    }

    @Test
    @DisplayName("Cuando la ocupación alcanza la capacidad declarada, el local queda marcado como lleno")
    void obtenerCapacidad_conOcupacionIgualALaCapacidad_localQuedaLleno() {
        LocalRecepcion local = localConCapacidad(5.0);
        when(localRepository.findById(ID_LOCAL)).thenReturn(Optional.of(local));
        when(donacionRepository.countByLocalRecepcionIdLocalAndEstadoActual(ID_LOCAL, "EN_ALMACEN")).thenReturn(5L);

        LocalCapacidadResponseDTO capacidad = localService.obtenerCapacidad(ID_LOCAL);

        assertThat(capacidad.getLleno()).isTrue();
    }

    @Test
    @DisplayName("estaLleno() refleja la misma regla que obtenerCapacidad() para bloquear la selección del local")
    void estaLleno_conOcupacionMayorALaCapacidad_devuelveTrue() {
        LocalRecepcion local = localConCapacidad(3.0);
        when(localRepository.findById(ID_LOCAL)).thenReturn(Optional.of(local));
        when(donacionRepository.countByLocalRecepcionIdLocalAndEstadoActual(ID_LOCAL, "EN_ALMACEN")).thenReturn(7L);

        assertThat(localService.estaLleno(ID_LOCAL)).isTrue();
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
