package com.donaciones.service;

import com.donaciones.dto.request.ActualizarEstadoRequest;
import com.donaciones.dto.response.DonacionResponse;
import com.donaciones.dto.response.TrackingResponseDTO;
import com.donaciones.entity.Donacion;
import com.donaciones.entity.HistorialEstado;
import com.donaciones.entity.LocalRecepcion;
import com.donaciones.entity.Usuario;
import com.donaciones.exception.ResourceNotFoundException;
import com.donaciones.repository.DonacionRepository;
import com.donaciones.repository.HistorialEstadoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Seguimiento de donaciones: línea de tiempo del donante")
class SeguimientoDonacionTest {

    private static final Integer ID_DONACION = 1;
    private static final Long ID_LOCAL = 2L;
    private static final String CODIGO = "DON-2026-A1B2C3";
    private static final String OBSERVACION_ALMACEN = "Donación verificada e ingresada al almacén central";

    @Mock
    private DonacionRepository donacionRepository;

    @Mock
    private HistorialEstadoRepository historialRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private DonacionServiceImpl donacionService;

    // HU-11: Línea de tiempo del seguimiento de la donación.
    @Test
    @DisplayName("Retorna la línea de tiempo con Registrado, En Almacén, En Tránsito y Entregado en orden cronológico")
    void obtenerSeguimiento_conDonacionEntregada_retornaTodasLasFasesEnOrdenCronologico() {
        LocalDateTime inicio = LocalDateTime.of(2026, 9, 30, 8, 0);
        Donacion donacion = donacion("ENTREGADO");
        when(donacionRepository.findByCodigoSeguimiento(CODIGO)).thenReturn(Optional.of(donacion));
        when(historialRepository.findByDonacionIdDonacionOrderByFechaCambioAsc(ID_DONACION))
                .thenReturn(List.of(
                        historial(donacion, "REGISTRADO", inicio, "Donación registrada por el donante", null),
                        historial(donacion, "EN_ALMACEN", inicio.plusHours(2), OBSERVACION_ALMACEN, localRecepcion()),
                        historial(donacion, "EN_TRANSITO", inicio.plusHours(5), "Donación en camino", null),
                        historial(donacion, "ENTREGADO", inicio.plusHours(9), "Entregada a los damnificados", null)));

        TrackingResponseDTO tracking = donacionService.obtenerSeguimiento(CODIGO);

        assertThat(tracking.getHistorial())
                .extracting("estado")
                .containsExactly("REGISTRADO", "EN_ALMACEN", "EN_TRANSITO", "ENTREGADO");
        assertThat(tracking.getHistorial())
                .extracting("fechaCambio")
                .containsExactly(inicio, inicio.plusHours(2), inicio.plusHours(5), inicio.plusHours(9));
        assertThat(tracking.getHistorial().get(1).getObservacion()).isEqualTo(OBSERVACION_ALMACEN);
        verify(historialRepository).findByDonacionIdDonacionOrderByFechaCambioAsc(ID_DONACION);
        verify(historialRepository, never()).findByDonacionIdDonacionOrderByFechaCambioDesc(any());
    }

    @Test
    @DisplayName("Identifica la fase actual: el estado actual coincide con la última fase de la línea de tiempo")
    void obtenerSeguimiento_conDonacionEnTransito_identificaLaFaseActualComoUltimaDelHistorial() {
        LocalDateTime inicio = LocalDateTime.of(2026, 9, 30, 8, 0);
        Donacion donacion = donacion("EN_TRANSITO");
        when(donacionRepository.findByCodigoSeguimiento(CODIGO)).thenReturn(Optional.of(donacion));
        when(historialRepository.findByDonacionIdDonacionOrderByFechaCambioAsc(ID_DONACION))
                .thenReturn(List.of(
                        historial(donacion, "REGISTRADO", inicio, null, null),
                        historial(donacion, "EN_ALMACEN", inicio.plusHours(2), null, localRecepcion()),
                        historial(donacion, "EN_TRANSITO", inicio.plusHours(5), null, null)));

        TrackingResponseDTO tracking = donacionService.obtenerSeguimiento(CODIGO);

        assertThat(tracking.getEstadoActual()).isEqualTo("EN_TRANSITO");
        assertThat(tracking.getHistorial()).hasSize(3);
        assertThat(tracking.getHistorial().getLast().getEstado()).isEqualTo(tracking.getEstadoActual());
    }

    @Test
    @DisplayName("No retorna la línea de tiempo cuando el código de seguimiento no existe")
    void obtenerSeguimiento_conCodigoInexistente_lanzaResourceNotFoundException() {
        when(donacionRepository.findByCodigoSeguimiento("DON-2026-ZZZZZZ")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> donacionService.obtenerSeguimiento("DON-2026-ZZZZZZ"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("No se encontró la donación con el código de seguimiento DON-2026-ZZZZZZ");

        verifyNoInteractions(historialRepository);
    }

    // HU-11: Cambios de estado reflejados en el seguimiento.
    @Test
    @DisplayName("Cambia el estado de Registrado a En Almacén, lo guarda y registra la nueva fase en el historial")
    void cambiarEstado_deRegistradoAEnAlmacen_actualizaEstadoYRegistraHistorial() {
        Donacion donacion = donacion("REGISTRADO");
        when(donacionRepository.findById(ID_DONACION)).thenReturn(Optional.of(donacion));
        when(donacionRepository.save(any(Donacion.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        DonacionResponse response = donacionService.cambiarEstado(ID_DONACION, cambioEstado("EN_ALMACEN"));

        assertThat(response.getEstadoActual()).isEqualTo("EN_ALMACEN");
        assertThat(donacion.getEstadoActual()).isEqualTo("EN_ALMACEN");
        verify(donacionRepository).save(donacion);

        ArgumentCaptor<HistorialEstado> historialCaptor = ArgumentCaptor.forClass(HistorialEstado.class);
        verify(historialRepository).save(historialCaptor.capture());
        assertThat(historialCaptor.getValue().getEstado()).isEqualTo("EN_ALMACEN");
        assertThat(historialCaptor.getValue().getDonacion()).isSameAs(donacion);
        assertThat(historialCaptor.getValue().getObservacionHistorial()).isEqualTo(OBSERVACION_ALMACEN);
    }

    @Test
    @DisplayName("No cambia el estado ni registra historial cuando la donación no existe")
    void cambiarEstado_conDonacionInexistente_lanzaResourceNotFoundException() {
        when(donacionRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> donacionService.cambiarEstado(99, cambioEstado("EN_ALMACEN")))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("No se encontró la donación con id 99");

        verify(donacionRepository, never()).save(any(Donacion.class));
        verify(historialRepository, never()).save(any(HistorialEstado.class));
        verifyNoInteractions(emailService);
    }

    private ActualizarEstadoRequest cambioEstado(String estado) {
        return ActualizarEstadoRequest.builder()
                .estado(estado)
                .observacionHistorial(OBSERVACION_ALMACEN)
                .build();
    }

    private Donacion donacion(String estadoActual) {
        return Donacion.builder()
                .idDonacion(ID_DONACION)
                .codigoSeguimiento(CODIGO)
                .fechaRegistro(LocalDateTime.of(2026, 9, 30, 8, 0))
                .estadoActual(estadoActual)
                .usuario(Usuario.builder()
                        .idUsuario(7L)
                        .nombreUsuario("María")
                        .apellidosUsuario("García López")
                        .correoUsuario("maria.garcia@example.com")
                        .build())
                .localRecepcion(localRecepcion())
                .build();
    }

    private HistorialEstado historial(
            Donacion donacion, String estado, LocalDateTime fechaCambio, String observacion, LocalRecepcion local) {
        return HistorialEstado.builder()
                .donacion(donacion)
                .estado(estado)
                .fechaCambio(fechaCambio)
                .observacionHistorial(observacion)
                .localRecepcion(local)
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
