package com.donaciones.service;

import com.donaciones.dto.request.CrearKitRequestDTO;
import com.donaciones.dto.request.InsumoKitRequestDTO;
import com.donaciones.dto.response.KitDetalleResponseDTO;
import com.donaciones.dto.response.KitResponseDTO;
import com.donaciones.entity.CategoriaInsumo;
import com.donaciones.entity.DetalleDonacion;
import com.donaciones.entity.DetalleKit;
import com.donaciones.entity.Kit;
import com.donaciones.entity.LocalRecepcion;
import com.donaciones.exception.BadRequestException;
import com.donaciones.exception.ResourceNotFoundException;
import com.donaciones.repository.CategoriaInsumoRepository;
import com.donaciones.repository.DetalleDonacionRepository;
import com.donaciones.repository.KitRepository;
import com.donaciones.repository.LocalRecepcionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Year;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class KitServiceImpl implements KitService {

    private static final String ESTADO_EN_ALMACEN = "EN_ALMACEN";
    private static final String ESTADO_DISPONIBLE = "DISPONIBLE";

    private final KitRepository kitRepository;
    private final LocalRecepcionRepository localRepository;
    private final CategoriaInsumoRepository categoriaRepository;
    private final DetalleDonacionRepository detalleRepository;

    @Override
    @Transactional
    public List<KitResponseDTO> armarKits(CrearKitRequestDTO dto) {
        LocalRecepcion local = localRepository.findById(dto.getIdLocal())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró el local con id " + dto.getIdLocal()));

        Map<Integer, CategoriaInsumo> categorias = new HashMap<>();
        for (InsumoKitRequestDTO insumo : dto.getInsumosPorKit()) {
            CategoriaInsumo categoria = categoriaRepository.findById(insumo.getIdCategoria())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "No se encontró la categoría con id " + insumo.getIdCategoria()));
            categorias.put(categoria.getIdCategoria(), categoria);
        }

        Map<Integer, BigDecimal> necesariosPorCategoria = new HashMap<>();
        for (InsumoKitRequestDTO insumo : dto.getInsumosPorKit()) {
            BigDecimal totalInteres = insumo.getCantidad()
                    .multiply(BigDecimal.valueOf(dto.getCantidadKitsAFormar()))
                    .setScale(2, RoundingMode.HALF_UP);
            necesariosPorCategoria.put(insumo.getIdCategoria(), totalInteres);
        }

        Map<Integer, List<DetalleDonacion>> stockPorCategoria = new HashMap<>();
        for (Integer idCategoria : categorias.keySet()) {
            List<DetalleDonacion> detalles = ordenarFIFO(detalleRepository
                    .findByDonacionEstadoActualAndDonacionLocalRecepcionIdLocalAndCategoriaIdCategoria(
                            ESTADO_EN_ALMACEN, dto.getIdLocal(), idCategoria));
            stockPorCategoria.put(idCategoria, detalles);

            BigDecimal disponible = detalles.stream()
                    .filter(d -> d.getCantidadVerificada() != null)
                    .map(DetalleDonacion::getCantidadVerificada)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal necesario = necesariosPorCategoria.get(idCategoria);
            if (disponible.compareTo(necesario) < 0) {
                throw new BadRequestException("Stock insuficiente de "
                        + categorias.get(idCategoria).getNombreCategoria()
                        + " (disponible " + disponible.toPlainString()
                        + ", se requiere " + necesario.toPlainString() + ")");
            }
        }

        for (Map.Entry<Integer, List<DetalleDonacion>> entry : stockPorCategoria.entrySet()) {
            descontarStockFIFO(entry.getValue(), necesariosPorCategoria.get(entry.getKey()));
        }

        int kitsAFormar = dto.getCantidadKitsAFormar();
        List<Kit> kits = new ArrayList<>();
        for (int i = 0; i < kitsAFormar; i++) {
            Kit kit = Kit.builder()
                    .codigoKit(generarCodigoKit())
                    .nombreKit(dto.getNombreKit())
                    .estado(ESTADO_DISPONIBLE)
                    .localRecepcion(local)
                    .build();
            kit.prePersist();
            for (InsumoKitRequestDTO insumo : dto.getInsumosPorKit()) {
                DetalleKit detalleKit = DetalleKit.builder()
                        .cantidadInsumo(insumo.getCantidad().setScale(2, RoundingMode.HALF_UP))
                        .categoria(categorias.get(insumo.getIdCategoria()))
                        .build();
                kit.agregarDetalle(detalleKit);
            }
            kits.add(kit);
        }

        kitRepository.saveAll(kits);
        log.info("Se armaron {} kits '{}' en el local {}", kitsAFormar, dto.getNombreKit(), local.getNombreLocal());
        return kits.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<KitResponseDTO> listarKitsPorLocal(Long idLocal) {
        boolean existeLocal = localRepository.existsById(idLocal);
        if (!existeLocal) {
            throw new ResourceNotFoundException("No se encontró el local con id " + idLocal);
        }
        return kitRepository.findByLocalRecepcionIdLocal(idLocal).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public KitResponseDTO obtenerKitPorCodigo(String codigoKit) {
        Kit kit = kitRepository.findByCodigoKit(codigoKit)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró el kit con código " + codigoKit));
        return toResponse(kit);
    }

    private void descontarStockFIFO(List<DetalleDonacion> detalles, BigDecimal necesario) {
        if (necesario.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        BigDecimal porDescontar = necesario;
        for (DetalleDonacion detalle : detalles) {
            if (porDescontar.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }
            if (detalle.getCantidadVerificada() == null
                    || detalle.getCantidadVerificada().compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            BigDecimal aDescontar = porDescontar.min(detalle.getCantidadVerificada());
            detalle.setCantidadVerificada(
                    detalle.getCantidadVerificada().subtract(aDescontar).setScale(2, RoundingMode.HALF_UP));
            porDescontar = porDescontar.subtract(aDescontar);
        }
    }

    private List<DetalleDonacion> ordenarFIFO(List<DetalleDonacion> detalles) {
        return detalles.stream()
                .sorted(Comparator.comparing(DetalleDonacion::getFechaVencimiento,
                                Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(DetalleDonacion::getIdDetalle))
                .collect(Collectors.toList());
    }

    private String generarCodigoKit() {
        String codigo;
        do {
            String aleatorio = UUID.randomUUID().toString()
                    .replace("-", "")
                    .substring(0, 6)
                    .toUpperCase();
            codigo = "KIT-" + Year.now() + "-" + aleatorio;
        } while (kitRepository.existsByCodigoKit(codigo));
        return codigo;
    }

    private KitResponseDTO toResponse(Kit kit) {
        List<KitDetalleResponseDTO> detalles = new ArrayList<>();
        if (kit.getDetalles() != null) {
            for (DetalleKit detalle : kit.getDetalles()) {
                CategoriaInsumo categoria = detalle.getCategoria();
                detalles.add(KitDetalleResponseDTO.builder()
                        .idDetalleKit(detalle.getIdDetalleKit())
                        .idCategoria(categoria.getIdCategoria())
                        .nombreCategoria(categoria.getNombreCategoria())
                        .unidadMedida(categoria.getUnidadMedCate())
                        .cantidadInsumo(detalle.getCantidadInsumo())
                        .build());
            }
        }
        return KitResponseDTO.builder()
                .idKit(kit.getIdKit())
                .codigoKit(kit.getCodigoKit())
                .nombreKit(kit.getNombreKit())
                .estado(kit.getEstado())
                .fechaCreacion(kit.getFechaCreacion())
                .idLocal(kit.getLocalRecepcion().getIdLocal())
                .nombreLocal(kit.getLocalRecepcion().getNombreLocal())
                .detalles(detalles)
                .build();
    }

}