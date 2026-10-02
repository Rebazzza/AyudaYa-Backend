package com.donaciones.service;

import com.donaciones.dto.request.CorroboracionRequestDTO;
import com.donaciones.dto.request.ItemCorroboracionRequestDTO;
import com.donaciones.dto.response.AlertaCaducidadDTO;
import com.donaciones.dto.response.DonacionResponse;
import com.donaciones.dto.response.ResumenInventarioDTO;
import com.donaciones.entity.CategoriaInsumo;
import com.donaciones.entity.DetalleDonacion;
import com.donaciones.entity.Donacion;
import com.donaciones.entity.HistorialEstado;
import com.donaciones.entity.LocalRecepcion;
import com.donaciones.entity.Notificacion;
import com.donaciones.entity.Trabajador;
import com.donaciones.entity.Usuario;
import com.donaciones.exception.BadRequestException;
import com.donaciones.exception.ResourceNotFoundException;
import com.donaciones.repository.DetalleDonacionRepository;
import com.donaciones.repository.DonacionRepository;
import com.donaciones.repository.HistorialEstadoRepository;
import com.donaciones.repository.LocalRecepcionRepository;
import com.donaciones.repository.NotificacionRepository;
import com.donaciones.repository.TrabajadorRepository;
import com.donaciones.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AlmacenServiceImpl implements AlmacenService {

    private static final String ESTADO_EN_ALMACEN = "EN_ALMACEN";
    private static final String CONFORME = "Conforme";
    private static final String EXCEDENTE = "Excedente";
    private static final String FALTANTE = "Faltante";
    private static final long DIAS_ALERTA_CADUCIDAD = 15;

    private final DonacionRepository donacionRepository;
    private final LocalRecepcionRepository localRepository;
    private final TrabajadorRepository trabajadorRepository;
    private final DetalleDonacionRepository detalleRepository;
    private final HistorialEstadoRepository historialRepository;
    private final NotificacionRepository notificacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final DonacionService donacionService;

    @Override
    @Transactional
    public DonacionResponse corroborarRecepcion(CorroboracionRequestDTO dto) {
        Donacion donacion = donacionRepository.findById(dto.getIdDonacion())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró la donación con id " + dto.getIdDonacion()));
        LocalRecepcion local = findLocal(dto.getIdLocal());
        Trabajador trabajador = findTrabajador(dto.getIdTrabajador());

        Map<Integer, ItemCorroboracionRequestDTO> solicitados = dto.getDetalles().stream()
                .collect(Collectors.toMap(ItemCorroboracionRequestDTO::getIdDetalle, Function.identity()));

        for (DetalleDonacion detalle : donacion.getDetalles()) {
            if (!Boolean.TRUE.equals(detalle.getActivo())) {
                continue;
            }
            ItemCorroboracionRequestDTO item = solicitados.get(detalle.getIdDetalle());
            if (item == null) {
                throw new BadRequestException("Todos los insumos deben ser verificados");
            }
            detalle.setCantidadVerificada(item.getCantidadVerificada());
            detalle.setFechaVencimiento(item.getFechaVencimiento());
            detalle.setObservacionDetalle(clasificarIncidencia(item.getCantidadVerificada(), detalle.getCantidadDeclarada()));
        }

        donacion.setEstadoActual(ESTADO_EN_ALMACEN);
        donacion.setLocalRecepcion(local);
        donacionRepository.save(donacion);

        HistorialEstado historial = HistorialEstado.builder()
                .donacion(donacion)
                .localRecepcion(local)
                .trabajador(trabajador)
                .estado(ESTADO_EN_ALMACEN)
                .latitud(dto.getLatitud())
                .longitud(dto.getLongitud())
                .observacionHistorial(dto.getObservaciones())
                .build();
        historialRepository.save(historial);

        List<DetalleDonacion> activos = donacion.getDetalles().stream()
                .filter(detalle -> Boolean.TRUE.equals(detalle.getActivo()))
                .toList();
        long incidencias = activos.stream()
                .filter(detalle -> detalle.getObservacionDetalle() != null
                        && !CONFORME.equals(detalle.getObservacionDetalle()))
                .count();
        if (incidencias > activos.size() / 2.0) {
            notificarAdministradores(donacion);
        }

        return donacionService.getById(donacion.getIdDonacion());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResumenInventarioDTO> obtenerInventarioPorLocal(Long idLocal) {
        List<DetalleDonacion> detalles = detalleRepository
                .findByDonacionEstadoActualAndDonacionLocalRecepcionIdLocalAndActivoTrue(ESTADO_EN_ALMACEN, idLocal);
        return detalles.stream()
                .filter(detalle -> Boolean.TRUE.equals(detalle.getCategoria().getActivo()))
                .collect(Collectors.groupingBy(detalle -> detalle.getCategoria().getIdCategoria()))
                .values()
                .stream()
                .map(this::toResumenInventario)
                .sorted(Comparator.comparing(ResumenInventarioDTO::getNombreCategoria))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlertaCaducidadDTO> obtenerAlertasCaducidad(Long idLocal) {
        LocalDateTime ahora = LocalDateTime.now();
        return detalleRepository
                .findByDonacionEstadoActualAndDonacionLocalRecepcionIdLocalAndActivoTrue(ESTADO_EN_ALMACEN, idLocal)
                .stream()
                .filter(detalle -> Boolean.TRUE.equals(detalle.getCategoria().getActivo()))
                .filter(detalle -> detalle.getFechaVencimiento() != null && detalle.getCantidadVerificada() != null)
                .map(detalle -> {
                    long dias = ChronoUnit.DAYS.between(ahora, detalle.getFechaVencimiento());
                    return Map.entry(detalle, dias);
                })
                .filter(entry -> entry.getValue() < DIAS_ALERTA_CADUCIDAD)
                .map(entry -> AlertaCaducidadDTO.builder()
                        .idDonacion(entry.getKey().getDonacion().getIdDonacion())
                        .codigoSeguimiento(entry.getKey().getDonacion().getCodigoSeguimiento())
                        .nombreCategoria(entry.getKey().getCategoria().getNombreCategoria())
                        .cantidad(entry.getKey().getCantidadVerificada())
                        .fechaVencimiento(entry.getKey().getFechaVencimiento())
                        .diasParaCaducar(entry.getValue())
                        .build())
                .sorted(Comparator.comparing(AlertaCaducidadDTO::getFechaVencimiento))
                .toList();
    }

    private String clasificarIncidencia(BigDecimal verificada, BigDecimal declarada) {
        int comparacion = verificada.compareTo(declarada);
        if (comparacion > 0) {
            return EXCEDENTE;
        }
        if (comparacion < 0) {
            return FALTANTE;
        }
        return CONFORME;
    }

    private ResumenInventarioDTO toResumenInventario(List<DetalleDonacion> detalles) {
        CategoriaInsumo categoria = detalles.get(0).getCategoria();
        BigDecimal stock = detalles.stream()
                .map(DetalleDonacion::getCantidadVerificada)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long incidencias = detalles.stream()
                .filter(detalle -> detalle.getObservacionDetalle() != null
                        && !CONFORME.equals(detalle.getObservacionDetalle()))
                .count();
        return ResumenInventarioDTO.builder()
                .idCategoria(categoria.getIdCategoria())
                .nombreCategoria(categoria.getNombreCategoria())
                .unidadMedida(categoria.getUnidadMedCate())
                .stockTotalVerificado(stock)
                .totalItemsIncidencia(incidencias)
                .requiereRefrigeracion(categoria.getRefrigerar())
                .build();
    }

    private void notificarAdministradores(Donacion donacion) {
        String mensaje = "Alerta: La donación " + donacion.getCodigoSeguimiento()
                + " finalizó la corroboración con más del 50% de incidencias";
        List<Usuario> administradores = usuarioRepository.findByRolNombreRol("Administrador");
        for (Usuario administrador : administradores) {
            notificacionRepository.save(Notificacion.builder()
                    .usuario(administrador)
                    .donacion(donacion)
                    .mensajeNoti(mensaje)
                    .build());
        }
    }

    private LocalRecepcion findLocal(Long id) {
        return localRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el local con id " + id));
    }

    private Trabajador findTrabajador(Integer id) {
        return trabajadorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el trabajador con id " + id));
    }

}