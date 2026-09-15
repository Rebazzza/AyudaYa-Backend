package com.donaciones.service;

import com.donaciones.dto.request.RegistroMonetarioRequestDTO;
import com.donaciones.dto.response.DonacionMonetariaResponseDTO;
import com.donaciones.dto.response.TotalFondosDTO;
import com.donaciones.entity.DonacionMonetaria;
import com.donaciones.entity.Usuario;
import com.donaciones.exception.BadRequestException;
import com.donaciones.exception.DuplicateResourceException;
import com.donaciones.exception.ResourceNotFoundException;
import com.donaciones.repository.DonacionMonetariaRepository;
import com.donaciones.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class DonacionMonetariaServiceImpl implements DonacionMonetariaService {

    private static final Set<String> ESTADOS_PERMITIDOS = Set.of("VERIFICADO", "RECHAZADO");

    private final DonacionMonetariaRepository donacionMonetariaRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional
    public DonacionMonetariaResponseDTO registrarDonacionMonetaria(RegistroMonetarioRequestDTO dto) {
        Usuario usuario = usuarioRepository.findById(dto.getIdUsuario())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró el usuario con id " + dto.getIdUsuario()));

        if (donacionMonetariaRepository.findByNumeroOperacion(dto.getNumeroOperacion()).isPresent()) {
            throw new DuplicateResourceException(
                    "Ya existe una donación monetaria con el número de operación " + dto.getNumeroOperacion());
        }

        DonacionMonetaria donacionMonetaria = DonacionMonetaria.builder()
                .monto(dto.getMonto().setScale(2, RoundingMode.HALF_UP))
                .moneda(dto.getMoneda() != null && !dto.getMoneda().isBlank() ? dto.getMoneda().toUpperCase() : "PEN")
                .metodoPago(dto.getMetodoPago().toUpperCase())
                .numeroOperacion(dto.getNumeroOperacion())
                .comprobanteUrl(dto.getComprobanteUrl())
                .usuario(usuario)
                .build();
        donacionMonetaria.prePersist();

        DonacionMonetaria guardada = donacionMonetariaRepository.save(donacionMonetaria);
        log.info("Donación monetaria {} registrada (monto {}, método {}) en estado PENDIENTE",
                guardada.getIdDonacionMonetaria(), guardada.getMonto(), guardada.getMetodoPago());
        return toResponse(guardada);
    }

    @Override
    @Transactional
    public DonacionMonetariaResponseDTO verificarFondos(Integer idDonacionMonetaria, String estado) {
        DonacionMonetaria donacionMonetaria = donacionMonetariaRepository.findById(idDonacionMonetaria)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró la donación monetaria con id " + idDonacionMonetaria));

        String estadoNormalizado = estado == null ? "" : estado.trim().toUpperCase();
        if (!ESTADOS_PERMITIDOS.contains(estadoNormalizado)) {
            throw new BadRequestException(
                    "El estado debe ser VERIFICADO o RECHAZADO (se recibió: " + estado + ")");
        }

        donacionMonetaria.setEstadoVerificacion(estadoNormalizado);
        DonacionMonetaria guardada = donacionMonetariaRepository.save(donacionMonetaria);
        log.info("Donación monetaria {} marcada como {}", guardada.getIdDonacionMonetaria(), estadoNormalizado);
        return toResponse(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public TotalFondosDTO obtenerTotalFondosRecaudados() {
        List<DonacionMonetaria> verificadas = donacionMonetariaRepository.findByEstadoVerificacion("VERIFICADO");
        BigDecimal total = verificadas.stream()
                .map(DonacionMonetaria::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        return TotalFondosDTO.builder().totalRecaudadoPEN(total).build();
    }

    private DonacionMonetariaResponseDTO toResponse(DonacionMonetaria dm) {
        Usuario usuario = dm.getUsuario();
        String nombreDonante = usuario != null
                ? usuario.getNombreUsuario() + " " + (usuario.getApellidosUsuario() != null
                        ? usuario.getApellidosUsuario() : "")
                : null;
        return DonacionMonetariaResponseDTO.builder()
                .idDonacionMonetaria(dm.getIdDonacionMonetaria())
                .idUsuario(usuario != null ? usuario.getIdUsuario() : null)
                .nombreDonante(nombreDonante != null ? nombreDonante.trim() : null)
                .monto(dm.getMonto())
                .moneda(dm.getMoneda())
                .metodoPago(dm.getMetodoPago())
                .numeroOperacion(dm.getNumeroOperacion())
                .comprobanteUrl(dm.getComprobanteUrl())
                .estadoVerificacion(dm.getEstadoVerificacion())
                .fechaRegistro(dm.getFechaRegistro())
                .build();
    }

}