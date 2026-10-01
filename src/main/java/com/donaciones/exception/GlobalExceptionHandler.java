package com.donaciones.exception;

import com.donaciones.dto.ApiResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleResourceNotFound(ResourceNotFoundException ex,
                                                                      HttpServletRequest request) {
        return buildError(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleDuplicateResource(DuplicateResourceException ex,
                                                                        HttpServletRequest request) {
        return buildError(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleInvalidCredentials(InvalidCredentialsException ex,
                                                                         HttpServletRequest request) {
        return buildError(HttpStatus.UNAUTHORIZED, ex.getMessage(), request);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleBadRequest(BadRequestException ex, HttpServletRequest request) {
        return buildError(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                             HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return buildError(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleMessageNotReadable(HttpMessageNotReadableException ex,
                                                                         HttpServletRequest request) {
        return buildError(HttpStatus.BAD_REQUEST, "El cuerpo de la solicitud no es válido o está vacío", request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleDataIntegrity(DataIntegrityViolationException ex,
                                                                    HttpServletRequest request) {
        return buildError(HttpStatus.CONFLICT,
                "La operación no se pudo completar porque el registro está relacionado con otros datos", request);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleMaxUploadSize(MaxUploadSizeExceededException ex,
                                                                   HttpServletRequest request) {
        return buildError(HttpStatus.PAYLOAD_TOO_LARGE,
                "El archivo enviado supera el tamaño máximo permitido", request);
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleMissingPart(MissingServletRequestPartException ex,
                                                                  HttpServletRequest request) {
        return buildError(HttpStatus.BAD_REQUEST,
                "Falta el archivo requerido '" + ex.getRequestPartName() + "' en la solicitud", request);
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleMultipart(MultipartException ex, HttpServletRequest request) {
        log.error("Error al procesar el archivo enviado en la petición {}", request.getRequestURI(), ex);
        return buildError(HttpStatus.BAD_REQUEST,
                "No se pudo procesar el archivo enviado: verifique el tamaño y el formato", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleGenericException(Exception ex, HttpServletRequest request) {
        log.error("Error no controlado en la petición {}", request.getRequestURI(), ex);
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error interno en el servidor", request);
    }

    private ResponseEntity<ApiResponseDTO<Void>> buildError(HttpStatus status, String message,
                                                            HttpServletRequest request) {
        ApiResponseDTO<Void> body = ApiResponseDTO.error(status, message, request.getRequestURI(),
                status.getReasonPhrase());
        return ResponseEntity.status(status).body(body);
    }

}