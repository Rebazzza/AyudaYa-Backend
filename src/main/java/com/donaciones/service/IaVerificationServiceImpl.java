package com.donaciones.service;

import com.donaciones.dto.RespuestaIaDniDTO;
import com.donaciones.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class IaVerificationServiceImpl implements IaVerificationService {

    private static final String PARAMETRO_ARCHIVO = "file";
    private static final Set<String> TIPOS_PERMITIDOS = Set.of(
            "image/jpeg", "image/png", "image/webp");
    private static final String MENSAJE_SERVICIO_NO_DISPONIBLE =
            "El servicio de verificación de IA no está disponible temporalmente";

    private final RestClient iaRestClient;

    @Override
    public RespuestaIaDniDTO verificarFotoDni(MultipartFile archivo) {
        validarArchivo(archivo);

        byte[] bytes;
        try {
            bytes = archivo.getBytes();
        } catch (IOException e) {
            throw new BadRequestException("No se pudo leer el archivo de la foto de DNI");
        }

        try {
            RespuestaIaDniDTO respuesta = iaRestClient.post()
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(cuerpoMultipart(bytes, archivo.getOriginalFilename()))
                    .retrieve()
                    .body(RespuestaIaDniDTO.class);
            return respuesta != null ? respuesta : respuestaNoDisponible();
        } catch (RestClientException e) {
            log.error("Fallo la comunicación con el microservicio de IA: {}", e.getMessage());
            return respuestaNoDisponible();
        } finally {
            Arrays.fill(bytes, (byte) 0);
        }
    }

    private MultiValueMap<String, Object> cuerpoMultipart(byte[] bytes, String nombreOriginal) {
        ByteArrayResource recurso = new ByteArrayResource(bytes) {
            @Override
            public String getFilename() {
                return nombreOriginal != null ? nombreOriginal : "dni";
            }
        };
        MultiValueMap<String, Object> cuerpo = new LinkedMultiValueMap<>();
        cuerpo.add(PARAMETRO_ARCHIVO, recurso);
        return cuerpo;
    }

    private RespuestaIaDniDTO respuestaNoDisponible() {
        return RespuestaIaDniDTO.builder()
                .esValido(false)
                .confianza(0.0)
                .mensaje(MENSAJE_SERVICIO_NO_DISPONIBLE)
                .build();
    }

    private void validarArchivo(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new BadRequestException("La foto de DNI está vacía");
        }
        String contentType = archivo.getContentType();
        if (contentType == null || !TIPOS_PERMITIDOS.contains(contentType)) {
            throw new BadRequestException(
                    "Formato de imagen no permitido. Solo se aceptan JPG, PNG o WebP");
        }
    }

}
