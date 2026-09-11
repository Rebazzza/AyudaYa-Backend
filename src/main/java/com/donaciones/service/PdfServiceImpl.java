package com.donaciones.service;

import com.donaciones.entity.DetalleDonacion;
import com.donaciones.entity.Donacion;
import com.donaciones.entity.HistorialEstado;
import com.donaciones.exception.ResourceNotFoundException;
import com.donaciones.repository.DonacionRepository;
import com.donaciones.repository.HistorialEstadoRepository;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PdfServiceImpl implements PdfService {

    private static final Color NARANJA = new Color(0xFF, 0x77, 0x17);
    private static final Color GRIS = new Color(0x6B, 0x72, 0x80);
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final DonacionRepository donacionRepository;
    private final HistorialEstadoRepository historialRepository;
    private final QrCodeService qrCodeService;

    @Value("${app.portal.url:http://localhost:4200}")
    private String portalUrl;

    @Override
    @Transactional(readOnly = true)
    public byte[] generarComprobanteDonacionPdf(String codigoSeguimiento) {
        Donacion donacion = findByIdOrCodigoOrThrow(codigoSeguimiento);
        Document document = new Document(PageSize.A4, 36, 36, 48, 48);
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(document, salida);
            document.open();

            Paragraph marca = new Paragraph("AyudaYa", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 26, NARANJA));
            marca.setSpacingAfter(2);
            document.add(marca);

            Paragraph subtitulo = new Paragraph("COMPROBANTE DE DONACIÓN",
                    FontFactory.getFont(FontFactory.HELVETICA, 12, GRIS));
            subtitulo.setSpacingAfter(20);
            document.add(subtitulo);

            PdfPTable info = new PdfPTable(2);
            info.setWidths(new float[]{1.2f, 3f});
            info.setWidthPercentage(100);
            info.setSpacingAfter(18);
            addInfoRow(info, "Código de seguimiento", donacion.getCodigoSeguimiento());
            addInfoRow(info, "Donante", nombreDonante(donacion));
            addInfoRow(info, "Centro de acopio",
                    donacion.getLocalRecepcion().getNombreLocal() + " - " + donacion.getLocalRecepcion().getDireccionLocal());
            addInfoRow(info, "Estado actual", donacion.getEstadoActual());
            addInfoRow(info, "Fecha de registro", formatear(donacion.getFechaRegistro()));
            addInfoRow(info, "Fecha de verificación", formatear(donacion.getFechaVerificacion()));
            document.add(info);

            Paragraph tituloItems = titulo("Detalle de los insumos");
            document.add(tituloItems);

            PdfPTable tabla = new PdfPTable(5);
            tabla.setWidths(new float[]{2f, 2.6f, 1f, 1f, 1.6f});
            tabla.setWidthPercentage(100);
            tabla.setSpacingAfter(14);
            addHeaderCell(tabla, "Categoría");
            addHeaderCell(tabla, "Descripción");
            addHeaderCell(tabla, "Declarado");
            addHeaderCell(tabla, "Verificado");
            addHeaderCell(tabla, "Observación");
            for (DetalleDonacion detalle : donacion.getDetalles()) {
                addCell(tabla, detalle.getCategoria().getNombreCategoria());
                addCell(tabla, detalle.getDescripcionDetalle() != null ? detalle.getDescripcionDetalle() : "-");
                addCell(tabla, String.valueOf(detalle.getCantidadDeclarada()));
                addCell(tabla, detalle.getCantidadVerificada() != null
                        ? String.valueOf(detalle.getCantidadVerificada()) : "Pendiente");
                addCell(tabla, detalle.getObservacionDetalle() != null ? detalle.getObservacionDetalle() : "");
            }
            document.add(tabla);

            Paragraph tituloTimeline = titulo("Historial de movimientos");
            document.add(tituloTimeline);

            PdfPTable timeline = new PdfPTable(4);
            timeline.setWidths(new float[]{1.4f, 1.6f, 1.8f, 2.2f});
            timeline.setWidthPercentage(100);
            timeline.setSpacingAfter(14);
            addHeaderCell(timeline, "Estado");
            addHeaderCell(timeline, "Fecha y hora");
            addHeaderCell(timeline, "Local");
            addHeaderCell(timeline, "Observación");
            List<HistorialEstado> historial = historialRepository
                    .findByDonacionIdDonacionOrderByFechaCambioAsc(donacion.getIdDonacion());
            for (HistorialEstado historialEstado : historial) {
                addCell(timeline, historialEstado.getEstado());
                addCell(timeline, formatear(historialEstado.getFechaCambio()));
                addCell(timeline, historialEstado.getLocalRecepcion() != null
                        ? historialEstado.getLocalRecepcion().getNombreLocal() : "-");
                addCell(timeline, historialEstado.getObservacionHistorial() != null
                        ? historialEstado.getObservacionHistorial() : "");
            }
            document.add(timeline);

            Paragraph tituloQr = titulo("Código QR de seguimiento");
            document.add(tituloQr);

            byte[] qr = qrCodeService.generarImagenQR(
                    portalUrl + "/seguimiento/" + donacion.getCodigoSeguimiento(), 220, 220);
            Image qrImage = Image.getInstance(qr);
            qrImage.setAlignment(Element.ALIGN_CENTER);
            qrImage.setBorder(Rectangle.BOX);
            qrImage.setBorderWidth(1f);
            qrImage.setBorderColor(NARANJA);
            document.add(qrImage);

            Paragraph url = new Paragraph(donacion.getCodigoSeguimiento(),
                    FontFactory.getFont(FontFactory.HELVETICA, 10, GRIS));
            url.setAlignment(Element.ALIGN_CENTER);
            document.add(url);

            document.close();
            return salida.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo generar el comprobante PDF: " + e.getMessage(), e);
        } finally {
            document.close();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generarEtiquetasQrMasivasPdf(List<String> codigosSeguimiento) {
        Document document = new Document(PageSize.A4, 24, 24, 32, 32);
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(document, salida);
            document.open();

            PdfPTable grid = new PdfPTable(3);
            grid.setWidthPercentage(100);
            grid.setWidths(new float[]{1f, 1f, 1f});
            for (String codigo : codigosSeguimiento) {
                grid.addCell(buildEtiqueta(findByIdOrCodigoOrThrow(codigo)));
            }
            document.add(grid);

            document.close();
            return salida.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo generar las etiquetas QR: " + e.getMessage(), e);
        } finally {
            document.close();
        }
    }

    private PdfPCell buildEtiqueta(Donacion donacion) throws Exception {
        PdfPTable card = new PdfPTable(1);
        card.setWidthPercentage(100);

        card.addCell(blankText("AyudaYa", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, NARANJA)));
        card.addCell(blankText(donacion.getCodigoSeguimiento(),
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
        card.addCell(blankText(
                donacion.getLocalRecepcion() != null ? donacion.getLocalRecepcion().getNombreLocal() : "-",
                FontFactory.getFont(FontFactory.HELVETICA, 9, GRIS)));
        card.addCell(blankText("Fecha: " + formatear(donacion.getFechaRegistro()),
                FontFactory.getFont(FontFactory.HELVETICA, 9)));

        byte[] qr = qrCodeService.generarImagenQR(
                portalUrl + "/seguimiento/" + donacion.getCodigoSeguimiento(), 70, 70);
        Image qrImage = Image.getInstance(qr);
        PdfPCell qrCell = new PdfPCell(qrImage, true);
        qrCell.setPadding(4f);
        qrCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        card.addCell(qrCell);

        PdfPCell wrapper = new PdfPCell(card);
        wrapper.setBorder(Rectangle.BOX);
        wrapper.setBorderColor(NARANJA);
        wrapper.setPadding(6f);
        return wrapper;
    }

    private PdfPCell blankText(String texto, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPaddingTop(4f);
        cell.setPaddingBottom(2f);
        return cell;
    }

    private Paragraph titulo(String texto) {
        Paragraph paragraph = new Paragraph(texto,
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, NARANJA));
        paragraph.setSpacingBefore(10);
        paragraph.setSpacingAfter(6);
        return paragraph;
    }

    private void addInfoRow(PdfPTable tabla, String etiqueta, String valor) {
        PdfPCell labelCell = new PdfPCell(new Phrase(etiqueta,
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, GRIS)));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPadding(4f);
        PdfPCell valueCell = new PdfPCell(new Phrase(valor,
                FontFactory.getFont(FontFactory.HELVETICA, 10)));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPadding(4f);
        tabla.addCell(labelCell);
        tabla.addCell(valueCell);
    }

    private void addHeaderCell(PdfPTable tabla, String texto) {
        PdfPCell cell = new PdfPCell(new Phrase(texto,
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE)));
        cell.setBackgroundColor(NARANJA);
        cell.setPadding(6f);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        tabla.addCell(cell);
    }

    private void addCell(PdfPTable tabla, String texto) {
        PdfPCell cell = new PdfPCell(new Phrase(texto,
                FontFactory.getFont(FontFactory.HELVETICA, 9)));
        cell.setPadding(5f);
        tabla.addCell(cell);
    }

    private Donacion findByIdOrCodigoOrThrow(String codigoSeguimiento) {
        return donacionRepository.findByCodigoSeguimiento(codigoSeguimiento.trim())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró la donación con el código de seguimiento " + codigoSeguimiento));
    }

    private String nombreDonante(Donacion donacion) {
        return donacion.getUsuario().getNombreUsuario() + " " + donacion.getUsuario().getApellidosUsuario();
    }

    private String formatear(LocalDateTime fecha) {
        return fecha != null ? FORMATO_FECHA.format(fecha) : "-";
    }

}