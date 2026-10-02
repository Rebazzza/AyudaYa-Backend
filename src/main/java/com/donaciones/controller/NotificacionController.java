package com.donaciones.controller;

import com.donaciones.dto.ApiResponseDTO;
import com.donaciones.dto.request.NotificacionRequest;
import com.donaciones.dto.response.NotificacionResponse;
import com.donaciones.service.NotificacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notificaciones")
@RequiredArgsConstructor
@Tag(name = "Notificaciones", description = "Gestión de notificaciones a usuarios")
public class NotificacionController {

    private final NotificacionService notificacionService;

    @GetMapping
    @Operation(summary = "Listar notificaciones",
            description = "Lista notificaciones. Opcionalmente filtra por idUsuario destinatario.")
    public ResponseEntity<ApiResponseDTO<List<NotificacionResponse>>> list(@RequestParam(required = false) Long idUsuario,
                                                                           HttpServletRequest servletRequest) {
        List<NotificacionResponse> notificaciones = notificacionService.list(idUsuario);
        ApiResponseDTO<List<NotificacionResponse>> body = ApiResponseDTO.success(HttpStatus.OK,
                "Notificaciones obtenidas exitosamente", servletRequest.getRequestURI(), notificaciones);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener notificación por ID", description = "Devuelve una notificación.")
    public ResponseEntity<ApiResponseDTO<NotificacionResponse>> getById(@PathVariable Integer id,
                                                                        HttpServletRequest servletRequest) {
        NotificacionResponse notificacion = notificacionService.getById(id);
        ApiResponseDTO<NotificacionResponse> body = ApiResponseDTO.success(HttpStatus.OK,
                "Notificación obtenida exitosamente", servletRequest.getRequestURI(), notificacion);
        return ResponseEntity.ok(body);
    }

    @PostMapping
    @Operation(summary = "Registrar notificación", description = "Crea una notificación para un usuario.")
    public ResponseEntity<ApiResponseDTO<NotificacionResponse>> create(@Valid @RequestBody NotificacionRequest request,
                                                                       HttpServletRequest servletRequest) {
        NotificacionResponse notificacion = notificacionService.create(request);
        ApiResponseDTO<NotificacionResponse> body = ApiResponseDTO.success(HttpStatus.CREATED,
                "Notificación creada exitosamente", servletRequest.getRequestURI(), notificacion);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @PutMapping("/{id}/leido")
    @Operation(summary = "Marcar notificación como leída", description = "Marca una notificación como leída (leido = true).")
    public ResponseEntity<ApiResponseDTO<NotificacionResponse>> marcarLeido(@PathVariable Integer id,
                                                                            HttpServletRequest servletRequest) {
        NotificacionResponse notificacion = notificacionService.marcarLeido(id);
        ApiResponseDTO<NotificacionResponse> body = ApiResponseDTO.success(HttpStatus.OK,
                "Notificación marcada como leída", servletRequest.getRequestURI(), notificacion);
        return ResponseEntity.ok(body);
    }

    @PutMapping("/usuario/{idUsuario}/leido-todo")
    @Operation(summary = "Marcar todas las notificaciones de un usuario como leídas",
            description = "Marca como leídas (leido = true) todas las notificaciones pendientes del usuario indicado. Devuelve la cantidad de notificaciones actualizadas.")
    public ResponseEntity<ApiResponseDTO<Integer>> marcarTodoLeido(@PathVariable Long idUsuario,
                                                                    HttpServletRequest servletRequest) {
        int actualizadas = notificacionService.marcarTodoLeido(idUsuario);
        ApiResponseDTO<Integer> body = ApiResponseDTO.success(HttpStatus.OK,
                "Notificaciones marcadas como leídas", servletRequest.getRequestURI(), actualizadas);
        return ResponseEntity.ok(body);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar notificación", description = "Elimina una notificación.")
    public ResponseEntity<ApiResponseDTO<Void>> delete(@PathVariable Integer id, HttpServletRequest servletRequest) {
        notificacionService.delete(id);
        ApiResponseDTO<Void> body = ApiResponseDTO.success(HttpStatus.OK,
                "Notificación eliminada exitosamente", servletRequest.getRequestURI(), null);
        return ResponseEntity.ok(body);
    }

}