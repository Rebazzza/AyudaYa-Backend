package com.donaciones.controller;

import com.donaciones.dto.ApiResponseDTO;
import com.donaciones.dto.request.NotificacionTestRequestDTO;
import com.donaciones.exception.BadRequestException;
import com.donaciones.service.EmailService;
import com.donaciones.service.PdfService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/documentos")
@RequiredArgsConstructor
@Tag(name = "Documentos", description = "Generación de comprobantes PDF, etiquetas QR y notificaciones por correo")
public class DocumentoController {

    private final PdfService pdfService;
    private final EmailService emailService;

    @GetMapping(value = "/comprobante/{codigoSeguimiento}", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Comprobante de donación en PDF",
            description = "Genera el comprobante oficial de la donación con sus detalles, timeline de movimientos y "
                    + "código QR empotrado para el seguimiento.")
    public ResponseEntity<byte[]> comprobante(@PathVariable String codigoSeguimiento) {
        byte[] pdf = pdfService.generarComprobanteDonacionPdf(codigoSeguimiento);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=comprobante-" + codigoSeguimiento + ".pdf")
                .body(pdf);
    }

    @PostMapping(value = "/etiquetas-qr", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Etiquetas QR masivas en PDF",
            description = "Recibe una lista de códigos de seguimiento y genera una hoja imprimible con una etiqueta "
                    + "QR por caja (cuadrícula 3x4).")
    public ResponseEntity<byte[]> etiquetasQr(@RequestBody List<String> codigosSeguimiento) {
        if (codigosSeguimiento == null || codigosSeguimiento.isEmpty()) {
            throw new BadRequestException("Debe enviar al menos un código de seguimiento");
        }
        byte[] pdf = pdfService.generarEtiquetasQrMasivasPdf(codigosSeguimiento);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=etiquetas-qr.pdf")
                .body(pdf);
    }

    @PostMapping(value = "/notificar-test", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Probar envío de correo de notificación",
            description = "Utilidad para validar la plantilla y el envío SMTP simulando un cambio de estado.")
    public ResponseEntity<ApiResponseDTO<Void>> notificarTest(@Valid @RequestBody NotificacionTestRequestDTO request,
                                                              HttpServletRequest servletRequest) {
        emailService.enviarNotificacionEstado(request.getCorreoDestino(), request.getNombreDonante(),
                request.getCodigoSeguimiento(), request.getNuevoEstado(), request.getNombreLocal());
        ApiResponseDTO<Void> body = ApiResponseDTO.success(HttpStatus.OK,
                "Correo de notificación enviado exitosamente", servletRequest.getRequestURI(), null);
        return ResponseEntity.ok(body);
    }

}