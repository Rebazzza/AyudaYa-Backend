package com.donaciones.service;

import com.donaciones.exception.EmailException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String emailFrom;

    @Value("${app.portal.url:http://localhost:4200}")
    private String portalUrl;

    @Override
    public void enviarNotificacionEstado(String correoDestino, String nombreDonante, String codigoSeguimiento,
                                         String nuevoEstado, String nombreLocal) {
        try {
            Context context = new Context();
            context.setVariable("nombreDonante", nombreDonante);
            context.setVariable("codigoSeguimiento", codigoSeguimiento);
            context.setVariable("nuevoEstado", nuevoEstado);
            context.setVariable("nombreLocal", nombreLocal);
            context.setVariable("urlSeguimiento", portalUrl + "/seguimiento/" + codigoSeguimiento);

            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name());
            helper.setFrom(emailFrom);
            helper.setTo(correoDestino);
            helper.setSubject("AyudaYa - Donación " + codigoSeguimiento + " | " + nuevoEstado);
            helper.setText(templateEngine.process("email-estado", context), true);
            javaMailSender.send(message);
            log.info("Correo enviado a {} para la donación {}", correoDestino, codigoSeguimiento);
        } catch (Exception e) {
            log.error("Error enviando correo a {} para la donación {}: {}", correoDestino, codigoSeguimiento,
                    e.getMessage(), e);
            throw new EmailException("No se pudo enviar el correo de notificación: " + e.getMessage(), e);
        }
    }

}