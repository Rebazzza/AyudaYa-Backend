package com.donaciones.service;

public interface EmailService {

    void enviarNotificacionEstado(String correoDestino, String nombreDonante, String codigoSeguimiento,
                                  String nuevoEstado, String nombreLocal);

}