package com.donaciones.service;

import com.donaciones.dto.request.ActualizarEstadoRequest;
import com.donaciones.dto.request.ActualizarUbicacionRequestDTO;
import com.donaciones.dto.request.DetalleDonacionRequest;
import com.donaciones.dto.request.DetalleDonacionRequestDTO;
import com.donaciones.dto.request.DonacionRegistroRequestDTO;
import com.donaciones.dto.request.DonacionRequest;
import com.donaciones.dto.response.DetalleDonacionResponse;
import com.donaciones.dto.response.DonacionResponse;
import com.donaciones.dto.response.DonacionResponseDTO;
import com.donaciones.dto.response.TrackingHistorialDTO;
import com.donaciones.dto.response.TrackingResponseDTO;
import com.donaciones.dto.response.UbicacionActualResponseDTO;
import com.donaciones.entity.CategoriaInsumo;
import com.donaciones.entity.DetalleDonacion;
import com.donaciones.entity.Donacion;
import com.donaciones.entity.HistorialEstado;
import com.donaciones.entity.LocalRecepcion;
import com.donaciones.entity.Trabajador;
import com.donaciones.entity.Usuario;
import com.donaciones.exception.BadRequestException;
import com.donaciones.exception.DuplicateResourceException;
import com.donaciones.exception.ResourceNotFoundException;
import com.donaciones.repository.CategoriaInsumoRepository;
import com.donaciones.repository.DonacionRepository;
import com.donaciones.repository.HistorialEstadoRepository;
import com.donaciones.repository.LocalRecepcionRepository;
import com.donaciones.repository.TrabajadorRepository;
import com.donaciones.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DonacionServiceImpl implements DonacionService {

    private static final String ESTADO_REGISTRADO = "REGISTRADO";
    private static final String ESTADO_ANULADO = "ANULADO";

    private final DonacionRepository donacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final LocalRecepcionRepository localRepository;
    private final TrabajadorRepository trabajadorRepository;
    private final CategoriaInsumoRepository categoriaRepository;
    private final HistorialEstadoRepository historialRepository;
    private final EmailService emailService;

    private static final Logger log = LoggerFactory.getLogger(DonacionServiceImpl.class);

    @Override
    @Transactional(readOnly = true)
    public List<DonacionResponse> list(Integer idUsuario, String estado) {
        List<Donacion> donaciones;
        if (idUsuario != null && estado != null) {
            donaciones = donacionRepository.findByUsuarioIdUsuarioAndEstadoActual(idUsuario.longValue(), estado);
        } else if (idUsuario != null) {
            donaciones = donacionRepository.findByUsuarioIdUsuario(idUsuario.longValue());
        } else if (estado != null && !estado.isBlank()) {
            donaciones = donacionRepository.findByEstadoActual(estado);
        } else {
            donaciones = donacionRepository.findAll();
        }
        return donaciones.stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DonacionResponse getById(Integer id) {
        return toResponse(findByIdOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public DonacionResponse getByCodigo(String codigoSeguimiento) {
        return toResponse(findByIdOrCodigoOrThrow(codigoSeguimiento.trim()));
    }

    @Override
    @Transactional
    public DonacionResponse update(Integer id, DonacionRequest request) {
        Donacion donacion = findByIdOrThrow(id);
        if (donacionRepository.existsByCodigoSeguimientoAndIdDonacionNot(request.getCodigoSeguimiento(), id)) {
            throw new DuplicateResourceException(
                    "Ya existe otra donación con el código de seguimiento " + request.getCodigoSeguimiento());
        }

        donacion.setCodigoSeguimiento(request.getCodigoSeguimiento());
        donacion.setFechaExpiracion(request.getFechaExpiracion());
        donacion.setFechaVerificacion(request.getFechaVerificacion());
        donacion.setEstadoActual(request.getEstadoActual());
        donacion.setUsuario(findUsuario(request.getIdUsuario()));
        donacion.setLocalRecepcion(findLocal(request.getIdLocalRecepcion()));
        donacion.setTrabajador(request.getIdTrabajador() != null ? findTrabajador(request.getIdTrabajador()) : null);

        donacion.getDetalles().clear();
        request.getDetalles().forEach(detalleRequest -> donacion.addDetalle(buildDetalle(detalleRequest)));
        return toResponse(donacionRepository.save(donacion));
    }

    @Override
    @Transactional
    public DonacionResponse cambiarEstado(Integer id, ActualizarEstadoRequest request) {
        Donacion donacion = findByIdOrThrow(id);
        donacion.setEstadoActual(request.getEstado());
        registerHistorial(donacion, request.getEstado(), request.getObservacionHistorial(),
                request.getIdLocal(), request.getIdTrabajador());
        notificarEstadoPorCorreo(donacion, request.getEstado());
        return toResponse(donacionRepository.save(donacion));
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        Donacion donacion = findByIdOrThrow(id);
        donacionRepository.delete(donacion);
    }

    @Override
    @Transactional
    public DonacionResponseDTO registrarDonacion(DonacionRegistroRequestDTO dto) {
        Donacion donacion = Donacion.builder()
                .codigoSeguimiento(generarCodigoSeguimiento())
                .estadoActual(ESTADO_REGISTRADO)
                .usuario(findUsuario(dto.getIdUsuario()))
                .localRecepcion(findLocal(dto.getIdLocalRecepcion()))
                .build();
        dto.getDetalles().forEach(detalleRequest -> donacion.addDetalle(buildDetalle(detalleRequest)));
        donacionRepository.save(donacion);
        registerHistorial(donacion, ESTADO_REGISTRADO, "Donación registrada por el donante");
        return toResponseDto(donacion);
    }

    @Override
    @Transactional(readOnly = true)
    public TrackingResponseDTO obtenerSeguimiento(String codigoSeguimiento) {
        Donacion donacion = donacionRepository.findByCodigoSeguimiento(codigoSeguimiento)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró la donación con el código de seguimiento " + codigoSeguimiento));
        return toTracking(donacion);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DonacionResponseDTO> obtenerDonacionesPorUsuario(Long idUsuario) {
        return donacionRepository.findByUsuarioIdUsuarioOrderByFechaRegistroDesc(idUsuario).stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Override
    @Transactional
    public DonacionResponseDTO anularDonacion(Integer idDonacion, Long idUsuario) {
        Donacion donacion = findByIdOrThrow(idDonacion);
        if (!donacion.getUsuario().getIdUsuario().equals(idUsuario)) {
            throw new BadRequestException("La donación no pertenece al usuario indicado");
        }
        if (!ESTADO_REGISTRADO.equals(donacion.getEstadoActual())) {
            throw new BadRequestException(
                    "Solo se puede anular una donación en estado " + ESTADO_REGISTRADO
                            + ". Estado actual: " + donacion.getEstadoActual());
        }
        donacion.setEstadoActual(ESTADO_ANULADO);
        donacionRepository.save(donacion);
        registerHistorial(donacion, ESTADO_ANULADO, "Donación cancelada por el usuario");
        notificarEstadoPorCorreo(donacion, ESTADO_ANULADO);
        return toResponseDto(donacion);
    }

    @Override
    @Transactional
    public UbicacionActualResponseDTO actualizarUbicacionYEstado(ActualizarUbicacionRequestDTO dto) {
        Donacion donacion = findByIdOrCodigoOrThrow(dto.getCodigoSeguimiento());
        LocalRecepcion local = findLocal(dto.getIdLocalRecepcion());
        Trabajador trabajador = dto.getIdTrabajador() != null ? findTrabajador(dto.getIdTrabajador()) : null;

        donacion.setEstadoActual(dto.getNuevoEstado());
        donacion.setLocalRecepcion(local);
        if ("ENTREGADO".equalsIgnoreCase(dto.getNuevoEstado())) {
            donacion.setFechaVerificacion(LocalDateTime.now());
        }
        donacionRepository.save(donacion);

        HistorialEstado historial = HistorialEstado.builder()
                .donacion(donacion)
                .localRecepcion(local)
                .trabajador(trabajador)
                .estado(dto.getNuevoEstado())
                .latitud(dto.getLatitud())
                .longitud(dto.getLongitud())
                .observacionHistorial(dto.getObservacion())
                .fechaCambio(LocalDateTime.now())
                .build();
        historialRepository.save(historial);

        notificarEstadoPorCorreo(donacion, dto.getNuevoEstado());
        return toUbicacion(donacion, historial);
    }

    @Override
    @Transactional(readOnly = true)
    public UbicacionActualResponseDTO obtenerUbicacionActual(String codigoSeguimiento) {
        Donacion donacion = findByIdOrCodigoOrThrow(codigoSeguimiento);
        List<HistorialEstado> historial = historialRepository
                .findByDonacionIdDonacionOrderByFechaCambioDesc(donacion.getIdDonacion());
        HistorialEstado ultimo = historial.isEmpty() ? null : historial.get(0);
        return toUbicacion(donacion, ultimo);
    }

    private Donacion findByIdOrCodigoOrThrow(String codigoSeguimiento) {
        return donacionRepository.findByCodigoSeguimiento(codigoSeguimiento)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró la donación con el código de seguimiento " + codigoSeguimiento));
    }

    private void notificarEstadoPorCorreo(Donacion donacion, String nuevoEstado) {
        try {
            Usuario donante = donacion.getUsuario();
            emailService.enviarNotificacionEstado(
                    donante.getCorreoUsuario(),
                    donante.getNombreUsuario() + " " + donante.getApellidosUsuario(),
                    donacion.getCodigoSeguimiento(),
                    nuevoEstado,
                    donacion.getLocalRecepcion() != null ? donacion.getLocalRecepcion().getNombreLocal() : null);
        } catch (Exception e) {
            log.warn("No se pudo notificar por correo al donante de la donación {}: {}",
                    donacion.getCodigoSeguimiento(), e.getMessage());
        }
    }

    private UbicacionActualResponseDTO toUbicacion(Donacion donacion, HistorialEstado ultimo) {
        return UbicacionActualResponseDTO.builder()
                .codigoSeguimiento(donacion.getCodigoSeguimiento())
                .estadoActual(donacion.getEstadoActual())
                .nombreLocal(donacion.getLocalRecepcion().getNombreLocal())
                .direccionLocal(donacion.getLocalRecepcion().getDireccionLocal())
                .latitudGPS(ultimo != null ? ultimo.getLatitud() : null)
                .longitudGPS(ultimo != null ? ultimo.getLongitud() : null)
                .fechaUltimoEscaneo(ultimo != null ? ultimo.getFechaCambio() : null)
                .build();
    }

    private String generarCodigoSeguimiento() {
        String codigo;
        do {
            String aleatorio = UUID.randomUUID().toString()
                    .replace("-", "")
                    .substring(0, 6)
                    .toUpperCase();
            codigo = "DON-" + Year.now() + "-" + aleatorio;
        } while (donacionRepository.existsByCodigoSeguimiento(codigo));
        return codigo;
    }

    private Donacion findByIdOrThrow(Integer id) {
        return donacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la donación con id " + id));
    }

    private Usuario findUsuario(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el usuario con id " + id));
    }

    private LocalRecepcion findLocal(Long id) {
        return localRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el local con id " + id));
    }

    private Trabajador findTrabajador(Integer id) {
        return trabajadorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el trabajador con id " + id));
    }

    private DetalleDonacion buildDetalle(DetalleDonacionRequest request) {
        CategoriaInsumo categoria = categoriaRepository.findById(request.getIdCategoria())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró la categoría con id " + request.getIdCategoria()));
        return DetalleDonacion.builder()
                .categoria(categoria)
                .descripcionDetalle(request.getDescripcionDetalle())
                .cantidadDeclarada(request.getCantidadDeclarada())
                .cantidadVerificada(request.getCantidadVerificada())
                .fechaVencimiento(request.getFechaVencimiento())
                .build();
    }

    private DetalleDonacion buildDetalle(DetalleDonacionRequestDTO request) {
        CategoriaInsumo categoria = categoriaRepository.findById(request.getIdCategoria())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró la categoría con id " + request.getIdCategoria()));
        return DetalleDonacion.builder()
                .categoria(categoria)
                .descripcionDetalle(request.getDescripcionDetalle())
                .cantidadDeclarada(request.getCantidadDeclarada())
                .fechaVencimiento(request.getFechaVencimiento())
                .build();
    }

    private void registerHistorial(Donacion donacion, String estado, String observacion) {
        registerHistorial(donacion, estado, observacion, null, null);
    }

    private void registerHistorial(Donacion donacion, String estado, String observacion,
                                   Long idLocal, Integer idTrabajador) {
        HistorialEstado historial = HistorialEstado.builder()
                .donacion(donacion)
                .estado(estado)
                .observacionHistorial(observacion)
                .localRecepcion(idLocal != null ? findLocal(idLocal) : null)
                .trabajador(idTrabajador != null ? findTrabajador(idTrabajador) : null)
                .build();
        historialRepository.save(historial);
    }

    private DonacionResponse toResponse(Donacion donacion) {
        List<DetalleDonacionResponse> detalles = donacion.getDetalles().stream()
                .map(this::toDetalleResponse)
                .toList();

        return DonacionResponse.builder()
                .idDonacion(donacion.getIdDonacion())
                .codigoSeguimiento(donacion.getCodigoSeguimiento())
                .fechaRegistro(donacion.getFechaRegistro())
                .fechaExpiracion(donacion.getFechaExpiracion())
                .fechaVerificacion(donacion.getFechaVerificacion())
                .estadoActual(donacion.getEstadoActual())
                .idUsuario(donacion.getUsuario().getIdUsuario())
                .dniDonante(donacion.getUsuario().getDniUsuario())
                .nombreDonante(nombreCompleto(donacion.getUsuario()))
                .idLocalRecepcion(donacion.getLocalRecepcion().getIdLocal())
                .nombreLocal(donacion.getLocalRecepcion().getNombreLocal())
                .idTrabajador(donacion.getTrabajador() != null ? donacion.getTrabajador().getIdTrabajador() : null)
                .nombreTrabajador(donacion.getTrabajador() != null
                        ? nombreCompleto(donacion.getTrabajador().getUsuario())
                        : null)
                .detalles(detalles)
                .build();
    }

    private DonacionResponseDTO toResponseDto(Donacion donacion) {
        List<DetalleDonacionResponse> detalles = donacion.getDetalles().stream()
                .map(this::toDetalleResponse)
                .toList();

        return DonacionResponseDTO.builder()
                .idDonacion(donacion.getIdDonacion())
                .codigoSeguimiento(donacion.getCodigoSeguimiento())
                .fechaRegistro(donacion.getFechaRegistro())
                .estadoActual(donacion.getEstadoActual())
                .idUsuario(donacion.getUsuario().getIdUsuario())
                .nombreDonante(nombreCompleto(donacion.getUsuario()))
                .idLocalRecepcion(donacion.getLocalRecepcion().getIdLocal())
                .nombreLocal(donacion.getLocalRecepcion().getNombreLocal())
                .direccionLocal(donacion.getLocalRecepcion().getDireccionLocal())
                .detalles(detalles)
                .build();
    }

    private TrackingResponseDTO toTracking(Donacion donacion) {
        List<TrackingHistorialDTO> historial = historialRepository
                .findByDonacionIdDonacionOrderByFechaCambioAsc(donacion.getIdDonacion())
                .stream()
                .map(historialEntry -> TrackingHistorialDTO.builder()
                        .estado(historialEntry.getEstado())
                        .fechaCambio(historialEntry.getFechaCambio())
                        .observacion(historialEntry.getObservacionHistorial())
                        .nombreLocal(historialEntry.getLocalRecepcion() != null
                                ? historialEntry.getLocalRecepcion().getNombreLocal()
                                : null)
                        .build())
                .toList();

        return TrackingResponseDTO.builder()
                .codigoSeguimiento(donacion.getCodigoSeguimiento())
                .estadoActual(donacion.getEstadoActual())
                .nombreLocal(donacion.getLocalRecepcion().getNombreLocal())
                .direccionLocal(donacion.getLocalRecepcion().getDireccionLocal())
                .fechaRegistro(donacion.getFechaRegistro())
                .historial(historial)
                .build();
    }

    private DetalleDonacionResponse toDetalleResponse(DetalleDonacion detalle) {
        return DetalleDonacionResponse.builder()
                .idDetalle(detalle.getIdDetalle())
                .idCategoria(detalle.getCategoria().getIdCategoria())
                .nombreCategoria(detalle.getCategoria().getNombreCategoria())
                .descripcionDetalle(detalle.getDescripcionDetalle())
                .cantidadDeclarada(detalle.getCantidadDeclarada())
                .cantidadVerificada(detalle.getCantidadVerificada())
                .fechaVencimiento(detalle.getFechaVencimiento())
                .observacionDetalle(detalle.getObservacionDetalle())
                .build();
    }

    private String nombreCompleto(Usuario usuario) {
        return usuario.getNombreUsuario() + " " + usuario.getApellidosUsuario();
    }

}