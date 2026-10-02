package com.donaciones.service;

import com.donaciones.dto.request.ActualizarUbicacionRequestDTO;
import com.donaciones.dto.request.DetalleDonacionRequestDTO;
import com.donaciones.dto.request.DonacionRegistroRequestDTO;
import com.donaciones.dto.response.DonacionResponse;
import com.donaciones.dto.response.DonacionResponseDTO;
import com.donaciones.entity.CategoriaInsumo;
import com.donaciones.entity.DetalleDonacion;
import com.donaciones.entity.Donacion;
import com.donaciones.entity.HistorialEstado;
import com.donaciones.entity.LocalRecepcion;
import com.donaciones.entity.Usuario;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Registro y consulta de donaciones del donante")
class DonacionServiceTest {

    private static final Long ID_USUARIO = 7L;
    private static final Long ID_LOCAL = 2L;

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

    // HU-04: Registro de donaciones con sus productos.
    @Test
    @DisplayName("Registra los productos con su cantidad y unidad, y guarda la donación con fecha y estado inicial")
    void registrarDonacion_conProductosValidos_guardaLaDonacionYRetornaSusDetalles() {
        Usuario usuario = usuarioDonante();
        LocalRecepcion local = localRecepcion();
        CategoriaInsumo arroz = categoria(1, "Arroz", "kg");
        CategoriaInsumo frazadas = categoria(2, "Frazadas", "unidades");
        AtomicInteger siguienteId = new AtomicInteger(1);

        when(usuarioRepository.findById(ID_USUARIO)).thenReturn(Optional.of(usuario));
        when(localRepository.findById(ID_LOCAL)).thenReturn(Optional.of(local));
        when(categoriaRepository.findById(1)).thenReturn(Optional.of(arroz));
        when(categoriaRepository.findById(2)).thenReturn(Optional.of(frazadas));
        when(donacionRepository.existsByCodigoSeguimiento(any())).thenReturn(false);
        when(donacionRepository.save(any(Donacion.class))).thenAnswer(invocacion -> {
            Donacion donacion = invocacion.getArgument(0);
            donacion.setIdDonacion(siguienteId.getAndIncrement());
            donacion.prePersist();
            return donacion;
        });

        DonacionResponseDTO response = donacionService.registrarDonacion(
                solicitud(detalle(1, "Arroz", "10"), detalle(2, "Frazadas", "5")));

        assertThat(response.getIdDonacion()).isEqualTo(1);
        assertThat(response.getCodigoSeguimiento()).matches("DON-\\d{4}-[A-F0-9]{6}");
        assertThat(response.getEstadoActual()).isEqualTo("REGISTRADO");
        assertThat(response.getIdUsuario()).isEqualTo(ID_USUARIO);
        assertThat(response.getNombreDonante()).isEqualTo("María García López");
        assertThat(response.getIdLocalRecepcion()).isEqualTo(ID_LOCAL);
        assertThat(response.getNombreLocal()).isEqualTo("Local Central");
        assertThat(response.getFechaRegistro()).isNotNull();
        assertThat(response.getDetalles()).hasSize(2);
        assertThat(response.getDetalles())
                .extracting("nombreCategoria")
                .containsExactly("Arroz", "Frazadas");
        assertThat(response.getDetalles())
                .extracting("cantidadDeclarada")
                .containsExactly(new BigDecimal("10"), new BigDecimal("5"));

        ArgumentCaptor<Donacion> donacionCaptor = ArgumentCaptor.forClass(Donacion.class);
        verify(donacionRepository).save(donacionCaptor.capture());
        Donacion guardada = donacionCaptor.getValue();
        assertThat(guardada.getEstadoActual()).isEqualTo("REGISTRADO");
        assertThat(guardada.getFechaRegistro()).isNotNull();
        assertThat(guardada.getDetalles())
                .extracting(detalle -> detalle.getCategoria().getUnidadMedCate())
                .containsExactly("kg", "unidades");
        assertThat(guardada.getDetalles())
                .extracting(detalle -> detalle.getDonacion())
                .containsOnly(guardada);

        ArgumentCaptor<HistorialEstado> historialCaptor = ArgumentCaptor.forClass(HistorialEstado.class);
        verify(historialRepository).save(historialCaptor.capture());
        assertThat(historialCaptor.getValue().getEstado()).isEqualTo("REGISTRADO");
        assertThat(historialCaptor.getValue().getObservacionHistorial())
                .isEqualTo("Donación registrada por el donante");
    }

    @Test
    @DisplayName("No registra la donación cuando alguna categoría de producto no existe")
    void registrarDonacion_conCategoriaInexistente_lanzaResourceNotFoundException() {
        when(usuarioRepository.findById(ID_USUARIO)).thenReturn(Optional.of(usuarioDonante()));
        when(localRepository.findById(ID_LOCAL)).thenReturn(Optional.of(localRecepcion()));
        when(categoriaRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> donacionService.registrarDonacion(
                solicitud(detalle(99, "Producto inexistente", "1"))))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("No se encontró la categoría con id 99");

        verify(donacionRepository, never()).save(any(Donacion.class));
        verify(historialRepository, never()).save(any(HistorialEstado.class));
    }

    // HU-05: Generación y validación del código de seguimiento.
    @Test
    @DisplayName("Registra una donación virtual y genera un código alfanumérico")
    void registrarDonacionVirtual_conProductosValidos_generaCodigoAlfanumerico() {
        prepararRegistroDonacion();

        DonacionResponseDTO response = donacionService.registrarDonacion(
                solicitud(detalle(1, "Arroz", "10")));

        assertThat(response.getCodigoSeguimiento()).matches("DON-\\d{4}-[A-F0-9]{6}");
    }

    @Test
    @DisplayName("Registra una donación presencial y genera un código alfanumérico")
    void registrarDonacionPresencial_conProductosValidos_generaCodigoAlfanumerico() {
        prepararRegistroDonacion();

        DonacionResponseDTO response = donacionService.registrarDonacion(
                solicitud(detalle(1, "Arroz", "10")));

        assertThat(response.getCodigoSeguimiento()).matches("DON-\\d{4}-[A-F0-9]{6}");
    }

    @Test
    @DisplayName("Valida un código vigente en el local físico y actualiza el estado de la donación")
    void actualizarUbicacionYEstado_conCodigoVigente_actualizaEstadoYRegistraHistorial() {
        LocalDateTime fechaRegistro = LocalDateTime.now().minusDays(1);
        Donacion donacion = donacionParaEscaneo(
                "DON-2026-A1B2C3", fechaRegistro, fechaRegistro.plusDays(5));
        when(donacionRepository.findByCodigoSeguimiento(donacion.getCodigoSeguimiento()))
                .thenReturn(Optional.of(donacion));
        when(localRepository.findById(ID_LOCAL)).thenReturn(Optional.of(localRecepcion()));
        when(donacionRepository.save(any(Donacion.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        donacionService.actualizarUbicacionYEstado(solicitudEscaneo(donacion.getCodigoSeguimiento()));

        assertThat(donacion.getEstadoActual()).isEqualTo("EN_ALMACEN");
        verify(donacionRepository).save(donacion);
        verify(historialRepository).save(any(HistorialEstado.class));
    }

    // HU-04: Consulta del historial de donaciones del donante.
    @Test
    @DisplayName("Lista las donaciones registradas para el usuario con sus productos")
    void obtenerDonacionesPorUsuario_conDonacionesRegistradas_retornaLaListaDelDonante() {
        CategoriaInsumo arroz = categoria(1, "Arroz", "kg");
        CategoriaInsumo frazadas = categoria(2, "Frazadas", "unidades");
        Donacion primera = donacionPersistida(1, "DON-2026-ABC123", arroz, "10");
        Donacion segunda = donacionPersistida(2, "DON-2026-DEF456", frazadas, "5");
        when(donacionRepository.findByUsuarioIdUsuarioOrderByFechaRegistroDesc(ID_USUARIO))
                .thenReturn(new ArrayList<>(List.of(segunda, primera)));

        List<DonacionResponseDTO> donaciones = donacionService.obtenerDonacionesPorUsuario(ID_USUARIO);

        assertThat(donaciones).hasSize(2);
        assertThat(donaciones)
                .extracting(DonacionResponseDTO::getCodigoSeguimiento)
                .containsExactly("DON-2026-DEF456", "DON-2026-ABC123");
        assertThat(donaciones)
                .extracting(donacion -> donacion.getDetalles().getFirst().getNombreCategoria())
                .containsExactly("Frazadas", "Arroz");
        verify(donacionRepository).findByUsuarioIdUsuarioOrderByFechaRegistroDesc(ID_USUARIO);
    }

    // HU-06: Consulta y verificación de donaciones en el local físico.
    @Test
    @DisplayName("Muestra donaciones de distintos donantes con el nombre de cada uno")
    void listarDonaciones_conDonantesDistintos_retornaCadaDonacionIdentificadaPorDonante() {
        Donacion donacionMaria = donacionHU06(1, "DON-2026-ABC123", usuarioHU06(7L, "María", "García"));
        Donacion donacionCarlos = donacionHU06(2, "DON-2026-DEF456", usuarioHU06(8L, "Carlos", "Pérez"));
        when(donacionRepository.findAll()).thenReturn(List.of(donacionMaria, donacionCarlos));

        List<DonacionResponse> donaciones = donacionService.list(null, null);

        assertThat(donaciones)
                .extracting(DonacionResponse::getNombreDonante)
                .containsExactly("María García", "Carlos Pérez");
        assertThat(donaciones)
                .extracting(DonacionResponse::getIdUsuario)
                .containsExactly(7L, 8L);
    }

    @Test
    @DisplayName("Busca la donación por el código ingresado y retorna la donación asociada")
    void obtenerDonacionPorCodigo_conCodigoExistente_retornaDonacionAsociada() {
        Donacion donacion = donacionHU06(3, "DON-2026-A1B2C3", usuarioHU06(8L, "Carlos", "Pérez"));
        donacion.addDetalle(detalleDonacionHU06(31, "Arroz", "10", "8"));
        when(donacionRepository.findByCodigoSeguimiento("DON-2026-A1B2C3"))
                .thenReturn(Optional.of(donacion));

        DonacionResponse response = donacionService.getByCodigo(" DON-2026-A1B2C3 ");

        assertThat(response.getCodigoSeguimiento()).isEqualTo("DON-2026-A1B2C3");
        assertThat(response.getNombreDonante()).isEqualTo("Carlos Pérez");
        assertThat(response.getDetalles()).hasSize(1);
        assertThat(response.getDetalles().getFirst().getIdDetalle()).isEqualTo(31);
        assertThat(response.getDetalles().getFirst().getCantidadVerificada())
                .isEqualByComparingTo("8");
        verify(donacionRepository).findByCodigoSeguimiento("DON-2026-A1B2C3");
    }

    @Test
    @DisplayName("Consulta nuevamente la donación y retorna la cantidad verificada actualizada")
    void obtenerDonacionPorId_conCantidadCorregida_retornaElDetalleActualizado() {
        Donacion donacion = donacionHU06(3, "DON-2026-A1B2C3", usuarioHU06(8L, "Carlos", "Pérez"));
        donacion.addDetalle(detalleDonacionHU06(31, "Arroz", "10", "8"));
        when(donacionRepository.findById(3)).thenReturn(Optional.of(donacion));

        DonacionResponse response = donacionService.getById(3);

        assertThat(response.getDetalles()).hasSize(1);
        assertThat(response.getDetalles().getFirst().getCantidadDeclarada())
                .isEqualByComparingTo("10");
        assertThat(response.getDetalles().getFirst().getCantidadVerificada())
                .isEqualByComparingTo("8");
    }

    private void prepararRegistroDonacion() {
        when(usuarioRepository.findById(ID_USUARIO)).thenReturn(Optional.of(usuarioDonante()));
        when(localRepository.findById(ID_LOCAL)).thenReturn(Optional.of(localRecepcion()));
        when(categoriaRepository.findById(1)).thenReturn(Optional.of(categoria(1, "Arroz", "kg")));
        when(donacionRepository.existsByCodigoSeguimiento(any())).thenReturn(false);
        when(donacionRepository.save(any(Donacion.class))).thenAnswer(invocacion -> {
            Donacion donacion = invocacion.getArgument(0);
            donacion.prePersist();
            return donacion;
        });
    }

    private DonacionRegistroRequestDTO solicitud(DetalleDonacionRequestDTO... detalles) {
        return DonacionRegistroRequestDTO.builder()
                .idUsuario(ID_USUARIO)
                .idLocalRecepcion(ID_LOCAL)
                .detalles(List.of(detalles))
                .build();
    }

    private ActualizarUbicacionRequestDTO solicitudEscaneo(String codigo) {
        return ActualizarUbicacionRequestDTO.builder()
                .codigoSeguimiento(codigo)
                .idLocalRecepcion(ID_LOCAL)
                .nuevoEstado("EN_ALMACEN")
                .build();
    }

    private DetalleDonacionRequestDTO detalle(Integer idCategoria, String descripcion, String cantidad) {
        return DetalleDonacionRequestDTO.builder()
                .idCategoria(idCategoria)
                .descripcionDetalle(descripcion)
                .cantidadDeclarada(new BigDecimal(cantidad))
                .build();
    }

    private CategoriaInsumo categoria(Integer id, String nombre, String unidad) {
        return CategoriaInsumo.builder()
                .idCategoria(id)
                .nombreCategoria(nombre)
                .unidadMedCate(unidad)
                .refrigerar(false)
                .build();
    }

    private Usuario usuarioDonante() {
        return Usuario.builder()
                .idUsuario(ID_USUARIO)
                .nombreUsuario("María")
                .apellidosUsuario("García López")
                .correoUsuario("maria.garcia@example.com")
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

    private Donacion donacionPersistida(
            Integer id, String codigo, CategoriaInsumo categoria, String cantidad) {
        Donacion donacion = Donacion.builder()
                .idDonacion(id)
                .codigoSeguimiento(codigo)
                .estadoActual("REGISTRADO")
                .usuario(usuarioDonante())
                .localRecepcion(localRecepcion())
                .build();
        donacion.addDetalle(com.donaciones.entity.DetalleDonacion.builder()
                .categoria(categoria)
                .descripcionDetalle(categoria.getNombreCategoria())
                .cantidadDeclarada(new BigDecimal(cantidad))
                .build());
        return donacion;
    }

    private Donacion donacionHU06(Integer id, String codigo, Usuario usuario) {
        return Donacion.builder()
                .idDonacion(id)
                .codigoSeguimiento(codigo)
                .estadoActual("REGISTRADO")
                .usuario(usuario)
                .localRecepcion(localRecepcion())
                .build();
    }

    private Usuario usuarioHU06(Long id, String nombre, String apellidos) {
        return Usuario.builder()
                .idUsuario(id)
                .nombreUsuario(nombre)
                .apellidosUsuario(apellidos)
                .dniUsuario("87654321")
                .build();
    }

    private DetalleDonacion detalleDonacionHU06(
            Integer id, String nombreCategoria, String declarada, String verificada) {
        return DetalleDonacion.builder()
                .idDetalle(id)
                .categoria(CategoriaInsumo.builder()
                        .idCategoria(1)
                        .nombreCategoria(nombreCategoria)
                        .unidadMedCate("kg")
                        .refrigerar(false)
                        .build())
                .cantidadDeclarada(new BigDecimal(declarada))
                .cantidadVerificada(new BigDecimal(verificada))
                .build();
    }

    private Donacion donacionParaEscaneo(
            String codigo, LocalDateTime fechaRegistro, LocalDateTime fechaExpiracion) {
        return Donacion.builder()
                .idDonacion(1)
                .codigoSeguimiento(codigo)
                .fechaRegistro(fechaRegistro)
                .fechaExpiracion(fechaExpiracion)
                .estadoActual("REGISTRADO")
                .usuario(usuarioDonante())
                .localRecepcion(localRecepcion())
                .build();
    }

}
