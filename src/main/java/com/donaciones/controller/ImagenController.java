package com.donaciones.controller;

import com.donaciones.dto.ApiResponseDTO;
import com.donaciones.dto.request.CargaImagenesDonacionRequestDTO;
import com.donaciones.dto.response.ImagenResponseDTO;
import com.donaciones.exception.BadRequestException;
import com.donaciones.service.ImagenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/imagenes")
@RequiredArgsConstructor
@Tag(name = "Imágenes", description = "Carga y consulta de imágenes de evidencia y verificación de DNI")
public class ImagenController {

    private final ImagenService imagenService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Subir una imagen",
            description = "Sube una única imagen (DNI o evidencia) con su tipo, asociada opcionalmente a una donación o usuario.")
    public ResponseEntity<ApiResponseDTO<ImagenResponseDTO>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("tipoImagen") String tipoImagen,
            @RequestParam(value = "idDonacion", required = false) Integer idDonacion,
            @RequestParam(value = "idUsuario", required = false) Long idUsuario,
            HttpServletRequest servletRequest) {
        ImagenResponseDTO imagen = imagenService.guardarImagen(file, tipoImagen, idDonacion, idUsuario);
        ApiResponseDTO<ImagenResponseDTO> body = ApiResponseDTO.success(HttpStatus.CREATED,
                "Imagen subida exitosamente", servletRequest.getRequestURI(), imagen);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @PostMapping(value = "/donacion/{idDonacion}/evidencias", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Subir evidencias de recepción",
            description = "Sube un máximo de 3 imágenes de evidencia de recepción asociadas a una donación.")
    public ResponseEntity<ApiResponseDTO<List<ImagenResponseDTO>>> subirEvidencias(
            @PathVariable Integer idDonacion,
            @RequestPart("files") List<MultipartFile> files,
            @ModelAttribute @Valid CargaImagenesDonacionRequestDTO request,
            HttpServletRequest servletRequest) {
        if (!idDonacion.equals(request.getIdDonacion())) {
            throw new BadRequestException("El id de donación de la URL no coincide con el del formulario");
        }
        List<ImagenResponseDTO> imagenes = imagenService.guardarEvidenciasDonacion(files, idDonacion);
        ApiResponseDTO<List<ImagenResponseDTO>> body = ApiResponseDTO.success(HttpStatus.CREATED,
                "Evidencias subidas exitosamente", servletRequest.getRequestURI(), imagenes);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @GetMapping("/donacion/{idDonacion}")
    @Operation(summary = "Imágenes de una donación",
            description = "Lista todas las imágenes asociadas a una donación con sus URLs accesibles.")
    public ResponseEntity<ApiResponseDTO<List<ImagenResponseDTO>>> porDonacion(@PathVariable Integer idDonacion,
                                                                               HttpServletRequest servletRequest) {
        List<ImagenResponseDTO> imagenes = imagenService.listarImagenesPorDonacion(idDonacion);
        ApiResponseDTO<List<ImagenResponseDTO>> body = ApiResponseDTO.success(HttpStatus.OK,
                "Imágenes obtenidas exitosamente", servletRequest.getRequestURI(), imagenes);
        return ResponseEntity.ok(body);
    }

    @DeleteMapping("/{idImagen}")
    @Operation(summary = "Eliminar imagen",
            description = "Elimina el archivo físico y el registro de la imagen (purga de DNI, evidencia).")
    public ResponseEntity<ApiResponseDTO<Void>> eliminar(@PathVariable Integer idImagen,
                                                         HttpServletRequest servletRequest) {
        imagenService.eliminarImagen(idImagen);
        ApiResponseDTO<Void> body = ApiResponseDTO.success(HttpStatus.OK,
                "Imagen eliminada exitosamente", servletRequest.getRequestURI(), null);
        return ResponseEntity.ok(body);
    }

}