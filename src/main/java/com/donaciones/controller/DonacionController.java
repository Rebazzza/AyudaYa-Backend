package com.donaciones.controller;

import com.donaciones.dto.ApiResponseDTO;
import com.donaciones.dto.request.ActualizarEstadoRequest;
import com.donaciones.dto.request.DonacionRegistroRequestDTO;
import com.donaciones.dto.request.DonacionRequest;
import com.donaciones.dto.response.DetalleDonacionResponse;
import com.donaciones.dto.response.DonacionResponse;
import com.donaciones.dto.response.DonacionResponseDTO;
import com.donaciones.dto.response.TrackingResponseDTO;
import com.donaciones.service.DonacionService;
import com.donaciones.service.HistorialEstadoService;
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

import com.donaciones.dto.response.HistorialEstadoResponse;

@RestController
@RequestMapping("/api/v1/donaciones")
@RequiredArgsConstructor
@Tag(name = "Donaciones", description = "Gestión del ciclo de vida de las donaciones")
public class DonacionController {

    private final DonacionService donacionService;
    private final HistorialEstadoService historialService;

    @GetMapping
    @Operation(summary = "Listar donaciones",
            description = "Lista donaciones. Opcionalmente filtra por idUsuario (donante) y/o estadoActual (CREADA, EN_TRANSITO, RECIBIDA, RECHAZADA).")
    public ResponseEntity<ApiResponseDTO<List<DonacionResponse>>> list(@RequestParam(required = false) Integer idUsuario,
                                                                       @RequestParam(required = false) String estado,
                                                                       HttpServletRequest servletRequest) {
        List<DonacionResponse> donaciones = donacionService.list(idUsuario, estado);
        ApiResponseDTO<List<DonacionResponse>> body = ApiResponseDTO.success(HttpStatus.OK,
                "Donaciones obtenidas exitosamente", servletRequest.getRequestURI(), donaciones);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener donación por ID", description = "Devuelve el detalle de una donación con sus artículos.")
    public ResponseEntity<ApiResponseDTO<DonacionResponse>> getById(@PathVariable Integer id,
                                                                    HttpServletRequest servletRequest) {
        DonacionResponse donacion = donacionService.getById(id);
        ApiResponseDTO<DonacionResponse> body = ApiResponseDTO.success(HttpStatus.OK,
                "Donación obtenida exitosamente", servletRequest.getRequestURI(), donacion);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/{id}/historial")
    @Operation(summary = "Historial de estados de una donación",
            description = "Devuelve el historial de cambios de estado de una donación (más reciente primero).")
    public ResponseEntity<ApiResponseDTO<List<HistorialEstadoResponse>>> historial(@PathVariable Integer id,
                                                                                   HttpServletRequest servletRequest) {
        List<HistorialEstadoResponse> historial = historialService.listByDonacion(id);
        ApiResponseDTO<List<HistorialEstadoResponse>> body = ApiResponseDTO.success(HttpStatus.OK,
                "Historial obtenido exitosamente", servletRequest.getRequestURI(), historial);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/tracking/{codigoSeguimiento}")
    @Operation(summary = "Consultar seguimiento por código",
            description = "Devuelve la línea de tiempo (historial de estados en orden cronológico) de una donación.")
    public ResponseEntity<ApiResponseDTO<TrackingResponseDTO>> tracking(@PathVariable String codigoSeguimiento,
                                                                        HttpServletRequest servletRequest) {
        TrackingResponseDTO seguimiento = donacionService.obtenerSeguimiento(codigoSeguimiento);
        ApiResponseDTO<TrackingResponseDTO> body = ApiResponseDTO.success(HttpStatus.OK,
                "Seguimiento obtenido exitosamente", servletRequest.getRequestURI(), seguimiento);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/codigo/{codigoSeguimiento}")
    @Operation(summary = "Buscar donación por código de seguimiento",
            description = "Devuelve la donación completa con sus detalles y cantidades declaradas, para prellenar la "
                    + "corroboración de recepción en el módulo de almacén.")
    public ResponseEntity<ApiResponseDTO<DonacionResponse>> porCodigo(@PathVariable String codigoSeguimiento,
                                                                      HttpServletRequest servletRequest) {
        DonacionResponse donacion = donacionService.getByCodigo(codigoSeguimiento);
        ApiResponseDTO<DonacionResponse> body = ApiResponseDTO.success(HttpStatus.OK,
                "Donación encontrada exitosamente", servletRequest.getRequestURI(), donacion);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/usuario/{idUsuario}")
    @Operation(summary = "Historial de donaciones de un donante",
            description = "Lista las donaciones de un donante en orden descendente por fecha de registro.")
    public ResponseEntity<ApiResponseDTO<List<DonacionResponseDTO>>> porUsuario(@PathVariable Long idUsuario,
                                                                                HttpServletRequest servletRequest) {
        List<DonacionResponseDTO> donaciones = donacionService.obtenerDonacionesPorUsuario(idUsuario);
        ApiResponseDTO<List<DonacionResponseDTO>> body = ApiResponseDTO.success(HttpStatus.OK,
                "Donaciones del donante obtenidas exitosamente", servletRequest.getRequestURI(), donaciones);
        return ResponseEntity.ok(body);
    }

    @PostMapping
    @Operation(summary = "Registrar donación",
            description = "Crea una donación (estado REGISTRADO) con código de seguimiento generado automáticamente y sus detalles.")
    public ResponseEntity<ApiResponseDTO<DonacionResponseDTO>> create(
            @Valid @RequestBody DonacionRegistroRequestDTO request,
            HttpServletRequest servletRequest) {
        DonacionResponseDTO donacion = donacionService.registrarDonacion(request);
        ApiResponseDTO<DonacionResponseDTO> body = ApiResponseDTO.success(HttpStatus.CREATED,
                "Donación registrada exitosamente", servletRequest.getRequestURI(), donacion);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar donación", description = "Actualiza la cabecera y reemplaza los detalles de la donación.")
    public ResponseEntity<ApiResponseDTO<DonacionResponse>> update(@PathVariable Integer id,
                                                                  @Valid @RequestBody DonacionRequest request,
                                                                  HttpServletRequest servletRequest) {
        DonacionResponse donacion = donacionService.update(id, request);
        ApiResponseDTO<DonacionResponse> body = ApiResponseDTO.success(HttpStatus.OK,
                "Donación actualizada exitosamente", servletRequest.getRequestURI(), donacion);
        return ResponseEntity.ok(body);
    }

    @PutMapping("/{id}/estado")
    @Operation(summary = "Cambiar estado de donación",
            description = "Actualiza el estadoActual de la donación y registra una entrada en el historial de estados.")
    public ResponseEntity<ApiResponseDTO<DonacionResponse>> cambiarEstado(@PathVariable Integer id,
                                                                          @Valid @RequestBody ActualizarEstadoRequest request,
                                                                          HttpServletRequest servletRequest) {
        DonacionResponse donacion = donacionService.cambiarEstado(id, request);
        ApiResponseDTO<DonacionResponse> body = ApiResponseDTO.success(HttpStatus.OK,
                "Estado de la donación actualizado", servletRequest.getRequestURI(), donacion);
        return ResponseEntity.ok(body);
    }

    @PutMapping("/{id}/anular")
    @Operation(summary = "Anular donación",
            description = "Anula una donación en estado REGISTRADO. Solo el donante dueño de la donación puede hacerlo.")
    public ResponseEntity<ApiResponseDTO<DonacionResponseDTO>> anular(@PathVariable Integer id,
                                                                     @RequestParam Long idUsuario,
                                                                     HttpServletRequest servletRequest) {
        DonacionResponseDTO donacion = donacionService.anularDonacion(id, idUsuario);
        ApiResponseDTO<DonacionResponseDTO> body = ApiResponseDTO.success(HttpStatus.OK,
                "Donación anulada exitosamente", servletRequest.getRequestURI(), donacion);
        return ResponseEntity.ok(body);
    }

    @DeleteMapping("/{id}/productos/{idDetalle}")
    @Operation(summary = "Dar de baja un producto donado",
            description = "Aplica baja lógica sobre un producto: cambia su estado a inactivo y deja de mostrarse, "
                    + "pero el registro se conserva en la base de datos.")
    public ResponseEntity<ApiResponseDTO<DetalleDonacionResponse>> darDeBajaProducto(
            @PathVariable Integer id,
            @PathVariable Integer idDetalle,
            HttpServletRequest servletRequest) {
        DetalleDonacionResponse producto = donacionService.darDeBajaProducto(id, idDetalle);
        ApiResponseDTO<DetalleDonacionResponse> body = ApiResponseDTO.success(HttpStatus.OK,
                "Producto dado de baja exitosamente", servletRequest.getRequestURI(), producto);
        return ResponseEntity.ok(body);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar donación", description = "Elimina una donación y sus detalles asociados.")
    public ResponseEntity<ApiResponseDTO<Void>> delete(@PathVariable Integer id, HttpServletRequest servletRequest) {
        donacionService.delete(id);
        ApiResponseDTO<Void> body = ApiResponseDTO.success(HttpStatus.OK,
                "Donación eliminada exitosamente", servletRequest.getRequestURI(), null);
        return ResponseEntity.ok(body);
    }

}