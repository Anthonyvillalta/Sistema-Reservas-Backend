package com.campestre.controllers;

import com.campestre.dto.response.NotificationResponse;
import com.campestre.services.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notificaciones")
public class NotificationController {

    private static final Logger log = LoggerFactory.getLogger(NotificationController.class);

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> findByReservation(@RequestParam Long reservaId) {
        return ResponseEntity.ok(notificationService.findByReservation(reservaId));
    }

    @PostMapping("/enviar-email")
    public ResponseEntity<?> sendEmail(@RequestParam Long reservaId) {
        try {
            NotificationResponse response = notificationService.sendEmailConfirmation(reservaId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error enviando email para reserva {}: {}", reservaId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "error", true,
                "mensaje", e.getMessage() != null ? e.getMessage() : "Error al enviar el correo"
            ));
        }
    }

    @PostMapping("/enviar-whatsapp")
    public ResponseEntity<NotificationResponse> sendWhatsApp(@RequestParam Long reservaId) {
        return ResponseEntity.ok(notificationService.sendWhatsApp(reservaId));
    }
}
