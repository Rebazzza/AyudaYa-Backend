package com.donaciones.service;

import com.donaciones.dto.response.DonacionResponseDTO;
import com.donaciones.entity.CategoriaInsumo;
import com.donaciones.entity.DetalleDonacion;
import com.donaciones.entity.Donacion;
import com.donaciones.entity.LocalRecepcion;
import com.donaciones.entity.Usuario;
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
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * HU-14: Historial personal de aportes (Juan Diego).
 * Casos de prueba cubiertos: CP-HU14.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Historial de donaciones de un usuario")
class DonacionServiceHistorialTest {

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

    private static final Long ID_USUARIO = 1L;
    private static final Long OTRO_USUARIO = 2L;

    @Test
    @DisplayName("Filtra automáticamente por el id_Usuario de la sesión actual, no trae donaciones de otro usuario")
    void obtenerDonacionesPorUsuario_consultaSoloLasDonacionesDelUsuarioIndicado() {
        when(donacionRepository.findByUsuarioIdUsuarioOrderByFechaRegistroDesc(ID_USUARIO)).thenReturn(List.of());

        donacionService.obtenerDonacionesPorUsuario(ID_USUARIO);

        ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
        verify(donacionRepository).findByUsuarioIdUsuarioOrderByFechaRegistroDesc(idCaptor.capture());
        assertThat(idCaptor.getValue()).isEqualTo(ID_USUARIO).isNotEqualTo(OTRO_USUARIO);
    }

    @Test
    @DisplayName("Cada fila muestra fechaRegistro, estadoActual y la cantidad de ítems (detalledonacion)")
    void obtenerDonacionesPorUsuario_cadaFilaMuestraFechaEstadoYCantidadDeItems() {
        Usuario usuario = Usuario.builder().idUsuario(ID_USUARIO).nombreUsuario("Ana").apellidosUsuario("Pérez").build();
        LocalRecepcion local = LocalRecepcion.builder().idLocal(1L).nombreLocal("Coliseo Manuel Bonilla")
                .direccionLocal("Av. Ejemplo 123").build();
        LocalDateTime fecha = LocalDateTime.of(2026, 3, 10, 9, 0);

        Donacion donacion = Donacion.builder()
                .idDonacion(10)
                .codigoSeguimiento("DON-2026-000010")
                .fechaRegistro(fecha)
                .estadoActual("REGISTRADO")
                .usuario(usuario)
                .localRecepcion(local)
                .build();
        donacion.addDetalle(detalle(1, donacion, "Conforme"));
        donacion.addDetalle(detalle(2, donacion, "Excedente"));

        when(donacionRepository.findByUsuarioIdUsuarioOrderByFechaRegistroDesc(ID_USUARIO))
                .thenReturn(List.of(donacion));

        List<DonacionResponseDTO> historial = donacionService.obtenerDonacionesPorUsuario(ID_USUARIO);

        assertThat(historial).hasSize(1);
        DonacionResponseDTO fila = historial.get(0);
        assertThat(fila.getFechaRegistro()).isEqualTo(fecha);
        assertThat(fila.getEstadoActual()).isEqualTo("REGISTRADO");
        assertThat(fila.getDetalles()).hasSize(2);
    }

    @Test
    @DisplayName("Ordena el historial de forma descendente, de la donación más reciente a la más antigua")
    void obtenerDonacionesPorUsuario_respetaElOrdenDescendenteDevueltoPorElRepositorio() {
        Usuario usuario = Usuario.builder().idUsuario(ID_USUARIO).build();
        LocalRecepcion local = LocalRecepcion.builder().idLocal(1L).nombreLocal("Centro Norte").build();

        Donacion masReciente = donacionSinDetalles(2, usuario, local, LocalDateTime.of(2026, 5, 1, 10, 0));
        Donacion masAntigua = donacionSinDetalles(1, usuario, local, LocalDateTime.of(2026, 1, 1, 10, 0));

        // El repositorio ya devuelve el orden descendente (findByUsuarioIdUsuarioOrderByFechaRegistroDesc);
        // el servicio no debe reordenar ni alterar esa secuencia al mapear a DTO.
        when(donacionRepository.findByUsuarioIdUsuarioOrderByFechaRegistroDesc(ID_USUARIO))
                .thenReturn(List.of(masReciente, masAntigua));

        List<DonacionResponseDTO> historial = donacionService.obtenerDonacionesPorUsuario(ID_USUARIO);

        assertThat(historial)
                .extracting(DonacionResponseDTO::getIdDonacion)
                .containsExactly(2, 1);
    }

    private Donacion donacionSinDetalles(Integer id, Usuario usuario, LocalRecepcion local, LocalDateTime fecha) {
        return Donacion.builder()
                .idDonacion(id)
                .codigoSeguimiento("DON-2026-00000" + id)
                .fechaRegistro(fecha)
                .estadoActual("REGISTRADO")
                .usuario(usuario)
                .localRecepcion(local)
                .build();
    }

    private DetalleDonacion detalle(Integer id, Donacion donacion, String observacion) {
        CategoriaInsumo categoria = CategoriaInsumo.builder()
                .idCategoria(1)
                .nombreCategoria("Alimentos")
                .unidadMedCate("kg")
                .build();
        return DetalleDonacion.builder()
                .idDetalle(id)
                .donacion(donacion)
                .categoria(categoria)
                .descripcionDetalle("Arroz")
                .cantidadDeclarada(new BigDecimal("10"))
                .cantidadVerificada(new BigDecimal("10"))
                .observacionDetalle(observacion)
                .build();
    }

}
