package com.donaciones.service;

import com.donaciones.entity.Notificacion;
import com.donaciones.entity.Usuario;
import com.donaciones.repository.DonacionRepository;
import com.donaciones.repository.NotificacionRepository;
import com.donaciones.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * HU-13: Gestión masiva de notificaciones (Juan Diego).
 * Casos de prueba cubiertos: CP-HU13.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Marcar todas las notificaciones de un usuario como leídas")
class NotificacionServiceTest {

    @Mock
    private NotificacionRepository notificacionRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private DonacionRepository donacionRepository;

    @InjectMocks
    private NotificacionService notificacionService;

    private static final Long ID_USUARIO = 1L;
    private static final Long OTRO_USUARIO = 2L;

    @Test
    @DisplayName("Con notificaciones pendientes, marca todas como leídas y devuelve la cantidad actualizada")
    void marcarTodoLeido_conNotificacionesPendientes_marcaTodasComoLeidasYRetornaLaCantidad() {
        Usuario usuario = Usuario.builder().idUsuario(ID_USUARIO).build();
        List<Notificacion> pendientes = List.of(
                notificacionPendiente(1, usuario),
                notificacionPendiente(2, usuario),
                notificacionPendiente(3, usuario)
        );
        when(notificacionRepository.findByUsuarioIdUsuarioAndLeidoFalse(ID_USUARIO)).thenReturn(pendientes);

        int actualizadas = notificacionService.marcarTodoLeido(ID_USUARIO);

        assertThat(actualizadas).isEqualTo(3);
        assertThat(pendientes).allMatch(Notificacion::getLeido);
        verify(notificacionRepository).saveAll(pendientes);
    }

    @Test
    @DisplayName("Solo afecta las notificaciones del id_Usuario indicado, no las de otros usuarios")
    void marcarTodoLeido_consultaSoloLasNotificacionesDelUsuarioIndicado() {
        when(notificacionRepository.findByUsuarioIdUsuarioAndLeidoFalse(ID_USUARIO)).thenReturn(List.of());

        notificacionService.marcarTodoLeido(ID_USUARIO);

        ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
        verify(notificacionRepository).findByUsuarioIdUsuarioAndLeidoFalse(idCaptor.capture());
        assertThat(idCaptor.getValue()).isEqualTo(ID_USUARIO).isNotEqualTo(OTRO_USUARIO);
    }

    @Test
    @DisplayName("Si el usuario no tiene notificaciones pendientes, no guarda nada y retorna 0")
    void marcarTodoLeido_sinNotificacionesPendientes_noGuardaNadaYRetornaCero() {
        when(notificacionRepository.findByUsuarioIdUsuarioAndLeidoFalse(ID_USUARIO)).thenReturn(List.of());

        int actualizadas = notificacionService.marcarTodoLeido(ID_USUARIO);

        assertThat(actualizadas).isZero();
        verify(notificacionRepository, never()).saveAll(org.mockito.ArgumentMatchers.anyList());
    }

    private Notificacion notificacionPendiente(Integer id, Usuario usuario) {
        return Notificacion.builder()
                .idNotificacion(id)
                .usuario(usuario)
                .mensajeNoti("Notificación " + id)
                .fechaEnvio(LocalDateTime.now())
                .leido(false)
                .build();
    }

}
