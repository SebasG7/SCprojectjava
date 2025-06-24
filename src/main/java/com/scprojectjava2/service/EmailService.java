package com.scprojectjava2.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    @Autowired
    private JavaMailSender mailSender;

    public void enviarFactura(String[] correos, String asunto, String mensaje) {
        for (String correo : correos) {
            SimpleMailMessage email = new SimpleMailMessage();
            email.setTo(correo);
            email.setSubject(asunto);
            email.setText(mensaje);
            mailSender.send(email);
        }
    }

    public void enviarFacturaConAdjunto(String[] correos, String asunto, String mensaje, byte[] pdfFactura, String nombreAdjunto) {
        if (pdfFactura == null || pdfFactura.length == 0) {
            logger.error("El PDF de la factura es nulo o está vacío. No se enviará el correo.");
            throw new IllegalArgumentException("El PDF de la factura es nulo o está vacío");
        }
        for (String correo : correos) {
            try {
                logger.info("Enviando factura a: {}", correo);
                MimeMessage mimeMessage = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
                helper.setTo(correo);
                helper.setSubject(asunto);
                helper.setText(mensaje, false);
                // Adjuntar el PDF
                helper.addAttachment(nombreAdjunto, new ByteArrayResource(pdfFactura));
                mailSender.send(mimeMessage);
                logger.info("Correo enviado correctamente a: {}", correo);
            } catch (Exception e) {
                logger.error("Error al enviar el correo a {}: {}", correo, e.getMessage(), e);
                throw new RuntimeException("Error al enviar el correo a " + correo + ": " + e.getMessage(), e);
            }
        }
    }
}