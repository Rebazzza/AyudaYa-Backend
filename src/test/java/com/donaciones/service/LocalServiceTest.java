package com.donaciones.service;

import com.donaciones.dto.request.LocalRequest;
import com.donaciones.dto.response.LocalResponse;
import com.donaciones.entity.LocalRecepcion;
import com.donaciones.repository.LocalRecepcionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Gestión y registro de locales de acopio")
class LocalServiceTest {

    private static final String NOMBRE = "Coliseo Manuel Bonilla";
    private static final String DIRECCION = "Av. Ejemplo 123";
    private static final String TELEFONO = "987654321";
    private static final double CAPACIDAD_MAXIMA = 100.0;

    @Mock
    private LocalRecepcionRepository localRepository;

    @InjectMocks
    private LocalService localService;

    @Test
    @DisplayName("Registra el local con dirección y capacidad máxima, y queda activo por defecto")
    void registrarLocal_guardaDireccionYCapacidadYActivaPorDefecto() {

        when(localRepository.save(any(LocalRecepcion.class))).thenAnswer(invocacion -> {
            LocalRecepcion local = invocacion.getArgument(0);
            local.setIdLocal(1L);
            return local;
        });

        LocalResponse response = localService.create(localNuevo());
        
        assertThat(response.getIdLocal()).isEqualTo(1L);
        assertThat(response.getDireccionLocal()).isEqualTo(DIRECCION);
        assertThat(response.getCapacidadLocalM3()).isEqualTo(CAPACIDAD_MAXIMA);
        assertThat(response.getTelefonoLocal()).isEqualTo(TELEFONO);
        assertThat(response.getEstadoActivo()).isTrue();
    }

    @Test
    @DisplayName("El local persistido conserva dirección, capacidad, teléfono y estadoActivo = true")
    void registrarLocal_persisteLaEntidadConEstadoActivoVerdadero() {
        when(localRepository.save(any(LocalRecepcion.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        localService.create(localNuevo());

        ArgumentCaptor<LocalRecepcion> captor = ArgumentCaptor.forClass(LocalRecepcion.class);
        verify(localRepository).save(captor.capture());

        LocalRecepcion persistido = captor.getValue();
        assertThat(persistido.getNombreLocal()).isEqualTo(NOMBRE);
        assertThat(persistido.getDireccionLocal()).isEqualTo(DIRECCION);
        assertThat(persistido.getCapacidadLocalM3()).isEqualTo(CAPACIDAD_MAXIMA);
        assertThat(persistido.getTelefonoLocal()).isEqualTo(TELEFONO);
        assertThat(persistido.getEstadoActivo()).isTrue();
    }

    @Test
    @DisplayName("La lista de locales activos no incluye locales inactivos")
    void listarLocales_soloExponeLosActivosAlDonante() {
        LocalRecepcion activo = localPersistido(1L, "Coliseo Manuel Bonilla", true);
        when(localRepository.findByEstadoActivoTrue()).thenReturn(List.of(activo));

        List<LocalResponse> locales = localService.listActive();

        assertThat(locales).hasSize(1);
        assertThat(locales.getFirst().getNombreLocal()).isEqualTo(NOMBRE);
        assertThat(locales.getFirst().getEstadoActivo()).isTrue();
    }

    @Test
    @DisplayName("La lista de selectable por el donante consulta únicamente los locales activos")
    void listarLocales_consultaSoloElFinderDeActivos() {
        when(localRepository.findByEstadoActivoTrue()).thenReturn(List.of());

        localService.listActive();

        verify(localRepository).findByEstadoActivoTrue();
        verify(localRepository, never()).findAll();
    }

    private LocalRequest localNuevo() {
        return LocalRequest.builder()
                .nombreLocal(NOMBRE)
                .direccionLocal(DIRECCION)
                .latitud(new BigDecimal("-12.04637400"))
                .longitud(new BigDecimal("-77.04279300"))
                .capacidadLocalM3(CAPACIDAD_MAXIMA)
                .telefonoLocal(TELEFONO)
                .build();
    }

    private LocalRecepcion localPersistido(Long id, String nombre, boolean activo) {
        return LocalRecepcion.builder()
                .idLocal(id)
                .nombreLocal(nombre)
                .direccionLocal(DIRECCION)
                .latitud(new BigDecimal("-12.04637400"))
                .longitud(new BigDecimal("-77.04279300"))
                .capacidadLocalM3(CAPACIDAD_MAXIMA)
                .telefonoLocal(TELEFONO)
                .estadoActivo(activo)
                .build();
    }

}