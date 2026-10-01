package com.donaciones.controller;

import com.donaciones.dto.ApiResponseDTO;
import com.donaciones.dto.VerificacionDniResponseDTO;
import com.donaciones.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/ia")
@RequiredArgsConstructor
@Tag(name = "Inteligencia Artificial", description = "Verificación biométrica de DNI mediante IA")
public class BiometriaIaController {

    private final UsuarioService usuarioService;

    @PostMapping(value = "/verificar-dni", consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Verificar la foto de un DNI con IA",
            description = "Envía la foto del DNI al microservicio de Inteligencia Artificial y actualiza el estado de "
                    + "verificación del donante. La imagen se procesa únicamente en memoria y se descarta al terminar: "
                    + "no se almacena en disco ni se registra en la base de datos (purga biométrica RF18).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dictamen emitido por el modelo de IA",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "400", description = "Archivo ausente, vacío o con formato no permitido",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "404", description = "El usuario indicado no existe",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "413", description = "El archivo supera el tamaño máximo permitido",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "500", description = "Error interno durante la verificación",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    public ResponseEntity<ApiResponseDTO<VerificacionDniResponseDTO>> verificarDni(
            @Parameter(description = "Identificador del usuario donante a verificar", example = "12", required = true,
                    in = ParameterIn.QUERY)
            @RequestParam("idUsuario") Integer idUsuario,
            @Parameter(description = "Fotografía del DNI (JPG, PNG o WebP)", required = true,
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
            @RequestParam("file") MultipartFile file,
            HttpServletRequest servletRequest) {
        VerificacionDniResponseDTO response = usuarioService.procesarVerificacionYPurgar(idUsuario, file);
        ApiResponseDTO<VerificacionDniResponseDTO> body = ApiResponseDTO.success(HttpStatus.OK,
                response.getDniVerificado() ? "DNI verificado exitosamente" : "DNI no verificado",
                servletRequest.getRequestURI(), response);
        return ResponseEntity.ok(body);
    }

}
