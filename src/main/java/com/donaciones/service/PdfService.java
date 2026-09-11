package com.donaciones.service;

import java.util.List;

public interface PdfService {

    byte[] generarComprobanteDonacionPdf(String codigoSeguimiento);

    byte[] generarEtiquetasQrMasivasPdf(List<String> codigosSeguimiento);

}