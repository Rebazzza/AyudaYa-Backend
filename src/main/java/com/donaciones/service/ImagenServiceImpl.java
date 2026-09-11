package com.donaciones.service;

import com.donaciones.dto.response.ImagenResponseDTO;
import com.donaciones.entity.Donacion;
import com.donaciones.entity.ImagenEvidencia;
import com.donaciones.entity.Usuario;
import com.donaciones.exception.BadRequestException;
import com.donaciones.exception.ResourceNotFoundException;
import com.donaciones.repository.DonacionRepository;
import com.donaciones.repository.ImagenEvidenciaRepository;
import com.donaciones.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImagenServiceImpl implements ImagenService {

    private static final int MAX_EVIDENCIAS_POR_DONACION = 3;
    private static final Set<String> TIPOS_PERMITIDOS = Set.of(
            "image/jpeg", "image/png", "image/webp");
    private static final Set<String> TIPOS_IMAGEN = Set.of(
            "DNI_FRONTAL", "DNI_POSTERIOR", "EVIDENCIA_RECEPCION", "EVIDENCIA_ENTREGA");

    private final ImagenEvidenciaRepository imagenRepository;
    private final DonacionRepository donacionRepository;
    private final UsuarioRepository usuarioRepository;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Override
    @Transactional
    public ImagenResponseDTO guardarImagen(MultipartFile archivo, String tipoImagen, Integer idDonacion, Long idUsuario) {
        validarArchivo(archivo);
        validarTipoImagen(tipoImagen);

        String nombreArchivo = almacenarArchivo(archivo);

        ImagenEvidencia imagen = ImagenEvidencia.builder()
                .nombreArchivo(nombreArchivo)
                .nombreOriginal(archivo.getOriginalFilename() != null
                        ? archivo.getOriginalFilename() : nombreArchivo)
                .rutaUrl("/uploads/" + nombreArchivo)
                .tipoImagen(tipoImagen)
                .tamanoBytes(archivo.getSize())
                .donacion(idDonacion != null ? findDonacion(idDonacion) : null)
                .usuario(idUsuario != null ? findUsuario(idUsuario) : null)
                .build();
        return toResponse(imagenRepository.save(imagen));
    }

    @Override
    @Transactional
    public List<ImagenResponseDTO> guardarEvidenciasDonacion(List<MultipartFile> archivos, Integer idDonacion) {
        if (archivos == null || archivos.isEmpty()) {
            throw new BadRequestException("Debe adjuntar al menos una imagen de evidencia");
        }
        if (archivos.size() > MAX_EVIDENCIAS_POR_DONACION) {
            throw new BadRequestException(
                    "No se pueden subir más de " + MAX_EVIDENCIAS_POR_DONACION
                            + " imágenes de evidencia simultáneamente");
        }
        List<ImagenResponseDTO> resultado = new ArrayList<>();
        for (MultipartFile archivo : archivos) {
            resultado.add(guardarImagen(archivo, "EVIDENCIA_RECEPCION", idDonacion, null));
        }
        return resultado;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ImagenResponseDTO> listarImagenesPorDonacion(Integer idDonacion) {
        return imagenRepository.findByDonacionIdDonacion(idDonacion).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void eliminarImagen(Integer idImagen) {
        ImagenEvidencia imagen = findByIdOrThrow(idImagen);
        Path rutaArchivo = Paths.get(uploadDir).resolve(imagen.getNombreArchivo()).normalize().toAbsolutePath();
        try {
            Files.deleteIfExists(rutaArchivo);
        } catch (IOException e) {
            log.warn("No se pudo eliminar el archivo físico {}: {}", rutaArchivo, e.getMessage());
        }
        imagenRepository.delete(imagen);
    }

    private void validarArchivo(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new BadRequestException("El archivo de imagen está vacío");
        }
        String contentType = archivo.getContentType();
        if (contentType == null || !TIPOS_PERMITIDOS.contains(contentType)) {
            throw new BadRequestException(
                    "Formato de imagen no permitido. Solo se aceptan JPG, PNG o WebP");
        }
    }

    private void validarTipoImagen(String tipoImagen) {
        if (tipoImagen == null || !TIPOS_IMAGEN.contains(tipoImagen)) {
            throw new BadRequestException(
                    "Tipo de imagen no válido. Valores permitidos: DNI_FRONTAL, DNI_POSTERIOR, "
                            + "EVIDENCIA_RECEPCION, EVIDENCIA_ENTREGA");
        }
    }

    private String almacenarArchivo(MultipartFile archivo) {
        try {
            Path directorio = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(directorio);

            String extension = extensionDe(archivo.getContentType());
            String nombreArchivo = UUID.randomUUID() + extension;
            Path rutaDestino = directorio.resolve(nombreArchivo);
            archivo.transferTo(rutaDestino.toFile());
            return nombreArchivo;
        } catch (IOException e) {
            throw new BadRequestException("No se pudo almacenar la imagen: " + e.getMessage());
        }
    }

    private String extensionDe(String contentType) {
        return switch (contentType) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };
    }

    private Donacion findDonacion(Integer idDonacion) {
        return donacionRepository.findById(idDonacion)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la donación con id " + idDonacion));
    }

    private Usuario findUsuario(Long idUsuario) {
        return usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el usuario con id " + idUsuario));
    }

    private ImagenEvidencia findByIdOrThrow(Integer idImagen) {
        return imagenRepository.findById(idImagen)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la imagen con id " + idImagen));
    }

    private ImagenResponseDTO toResponse(ImagenEvidencia imagen) {
        return ImagenResponseDTO.builder()
                .idImagen(imagen.getIdImagen())
                .nombreOriginal(imagen.getNombreOriginal())
                .rutaUrl(imagen.getRutaUrl())
                .tipoImagen(imagen.getTipoImagen())
                .fechaCarga(imagen.getFechaCarga())
                .build();
    }

}