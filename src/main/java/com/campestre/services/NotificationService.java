package com.campestre.services;

import com.campestre.dto.response.NotificationResponse;
import com.campestre.entities.Notification;
import com.campestre.entities.Reservation;
import com.campestre.enums.NotificationStatus;
import com.campestre.enums.NotificationType;
import com.campestre.exceptions.ResourceNotFoundException;
import com.campestre.repositories.NotificationRepository;
import com.campestre.repositories.ReservationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.mail.internet.MimeMessage;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final ReservationRepository reservationRepository;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String emailFrom;

    public NotificationService(NotificationRepository notificationRepository,
                               ReservationRepository reservationRepository,
                               JavaMailSender mailSender) {
        this.notificationRepository = notificationRepository;
        this.reservationRepository = reservationRepository;
        this.mailSender = mailSender;
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> findByReservation(Long reservationId) {
        return notificationRepository.findByReservationIdOrderByCreatedAtDesc(reservationId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public NotificationResponse sendEmailConfirmation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva", reservationId));

        String destinatario = reservation.getClient().getEmail();
        if (destinatario == null || destinatario.isBlank()) {
            throw new RuntimeException("El cliente no tiene email registrado");
        }

        String asunto = "✅ Reserva Confirmada - " + reservation.getCodigoReserva();
        String html = buildEmailHtml(reservation, false);

        return sendEmail(reservation, destinatario, asunto, html, "confirmación");
    }

    @Transactional
    public NotificationResponse sendCancellationEmail(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva", reservationId));

        String destinatario = reservation.getClient().getEmail();
        if (destinatario == null || destinatario.isBlank()) {
            throw new RuntimeException("El cliente no tiene email registrado");
        }

        String asunto = "❌ Reserva Cancelada - " + reservation.getCodigoReserva();
        String html = buildEmailHtml(reservation, true);

        return sendEmail(reservation, destinatario, asunto, html, "cancelación");
    }

    private NotificationResponse sendEmail(Reservation reservation, String destinatario, String asunto, String html, String tipo) {
        Notification notification = Notification.builder()
                .reservation(reservation)
                .tipo(NotificationType.EMAIL)
                .destinatario(destinatario)
                .asunto(asunto)
                .mensaje("Correo HTML enviado")
                .estadoEnvio(NotificationStatus.PENDIENTE)
                .build();

        try {
            MimeMessagePreparator preparator = (MimeMessage mime) -> {
                MimeMessageHelper helper = new MimeMessageHelper(mime, true, "UTF-8");
                helper.setFrom(emailFrom.isBlank() ? "noreply@campestre.com" : emailFrom);
                helper.setTo(destinatario);
                helper.setSubject(asunto);
                helper.setText(html, true);
            };
            mailSender.send(preparator);

            notification.setEstadoEnvio(NotificationStatus.ENVIADO);
            notification.setFechaEnvio(LocalDateTime.now());
            notification.setMensaje(html);
            log.info("Email de {} enviado a {} para reserva {}", tipo, destinatario, reservation.getCodigoReserva());
        } catch (Exception e) {
            notification.setEstadoEnvio(NotificationStatus.FALLIDO);
            log.error("Error al enviar email de {} a {}: {}", tipo, destinatario, e.getMessage());
        }

        notification = notificationRepository.save(notification);
        return toResponse(notification);
    }

    @Transactional
    public NotificationResponse sendWhatsApp(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva", reservationId));

        String destinatario = reservation.getClient().getCelular();
        String mensaje = buildWhatsAppMessage(reservation);

        Notification notification = Notification.builder()
                .reservation(reservation)
                .tipo(NotificationType.WHATSAPP)
                .destinatario(destinatario)
                .asunto("Reserva " + reservation.getCodigoReserva())
                .mensaje(mensaje)
                .estadoEnvio(NotificationStatus.PENDIENTE)
                .build();

        try {
            // Mock: Simular envío de WhatsApp
            notification.setEstadoEnvio(NotificationStatus.ENVIADO);
            notification.setFechaEnvio(LocalDateTime.now());
            log.info("WhatsApp enviado (mock) a {} para reserva {}", destinatario, reservation.getCodigoReserva());
        } catch (Exception e) {
            notification.setEstadoEnvio(NotificationStatus.FALLIDO);
            log.error("Error al enviar WhatsApp a {}: {}", destinatario, e.getMessage());
        }

        notification = notificationRepository.save(notification);
        return toResponse(notification);
    }

    private String buildEmailHtml(Reservation r) {
        return buildEmailHtml(r, false);
    }

    private String buildEmailHtml(Reservation r, boolean cancelado) {
        String nombre = r.getClient().getNombre();
        String codigo = r.getCodigoReserva();
        String ambiente = r.getEnvironment().getNombre();
        String fecha = r.getFechaEvento() != null ? r.getFechaEvento().format(DateTimeFormatter.ofPattern("dd 'de' MMMM 'del' yyyy")) : "Por confirmar";
        String horaInicio = r.getHoraInicio() != null ? r.getHoraInicio().toString() : "Todo el día";
        String horaFin = r.getHoraFin() != null ? r.getHoraFin().toString() : "";
        String horario = horaFin.isEmpty() ? horaInicio : horaInicio + " - " + horaFin;
        String total = String.format("S/ %.2f", r.getPrecioTotal());
        String estado = cancelado ? "CANCELADA" : r.getEstado().name();
        String celular = r.getClient().getCelular() != null ? r.getClient().getCelular().replaceAll("\\D", "") : "51999999999";
        String tipoEvento = r.getTipoEvento() != null && !r.getTipoEvento().isBlank() ? r.getTipoEvento() : "No especificado";
        boolean tieneSillas = r.getPrecioSillas() != null && r.getPrecioSillas().compareTo(BigDecimal.ZERO) > 0;
        boolean tieneMotor = r.getPrecioMotor() != null && r.getPrecioMotor().compareTo(BigDecimal.ZERO) > 0;
        String additionalRows = "";
        if (tieneSillas) {
            additionalRows += """
                      <tr><td colspan="2" style="border-top:1px solid #e5e7eb;"></td></tr>
                      <tr>
                        <td style="padding:7px 0;font-size:13px;color:#64748b;">Sillas</td>
                        <td style="padding:7px 0;font-size:14px;font-weight:600;color:#111827;">Sí</td>
                      </tr>
""";
        }
        if (tieneMotor) {
            additionalRows += """
                      <tr><td colspan="2" style="border-top:1px solid #e5e7eb;"></td></tr>
                      <tr>
                        <td style="padding:7px 0;font-size:13px;color:#64748b;">Motor</td>
                        <td style="padding:7px 0;font-size:14px;font-weight:600;color:#111827;">Sí</td>
                      </tr>
""";
        }

        String subtitulo = cancelado ? "José Antonio · Cancelación de reserva" : "José Antonio · Confirmación de reserva";
        String mensaje = cancelado
                ? "Tu reserva ha sido cancelada correctamente. Si esta decisión fue accidental o necesitas ayuda, puedes escribirnos por WhatsApp."
                : "¡Tu reserva está confirmada! 🎉 Hemos registrado correctamente tu reserva y este correo funciona como comprobante oficial para tu próximo evento.";
        String badgeText = cancelado ? "Cancelación registrada" : "Reserva confirmada";
        String headerColor = cancelado ? "#B42318" : "#0F4C81";
        String stateBadgeColor = cancelado ? "#DC2626" : "#2E8B57";
        String footerText = cancelado ? "Cancelación confirmada · Atención personalizada · Comprobante digital" : "Reserva confirmada · Atención personalizada · Comprobante digital";
        String actionText = cancelado ? "Si deseas reprogramar o solicitar ayuda, escríbenos por WhatsApp." : "Gracias por elegirnos. Para consultas, cambios o información adicional sobre tu reserva, estaremos disponibles por WhatsApp.";
        String icon = cancelado ? "❌" : "🏡";

        String template = """
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width,initial-scale=1.0">
  <meta http-equiv="X-UA-Compatible" content="IE=edge">
</head>
<body style="margin:0;padding:0;background-color:#f5f7fb;font-family:Segoe UI, Arial, sans-serif;color:#0f172a;">
  <table role="presentation" width="100%" cellpadding="0" cellspacing="0" style="background-color:#f5f7fb;padding:24px 0;">
    <tr>
      <td align="center">
        <table role="presentation" width="640" cellpadding="0" cellspacing="0" style="max-width:640px;width:100%;background-color:#ffffff;border-radius:24px;overflow:hidden;box-shadow:0 14px 38px rgba(15,23,42,0.10);">
          <tr>
            <td bgcolor="{{headerColor}}" style="background-color:{{headerColor}};padding:34px 32px 28px;text-align:center;">
              <div style="display:inline-flex;align-items:center;justify-content:center;width:64px;height:64px;border-radius:50%;background-color:#ffffff;margin-bottom:12px;">
                <span style="font-size:29px;">{{icon}}</span>
              </div>
              <h1 style="margin:0 0 6px;font-size:24px;line-height:1.2;color:#ffffff;font-weight:700;">Centro Recreacional Campestre</h1>
              <p style="margin:0;font-size:13px;letter-spacing:1.2px;color:#dce7f2;text-transform:uppercase;font-weight:600;">{{subtitulo}}</p>
            </td>
          </tr>
          <tr>
            <td style="padding:28px 32px 10px;">
              <p style="margin:0 0 8px;font-size:16px;color:#0f172a;">Hola <strong style="color:#0F4C81;">{{nombre}}</strong>,</p>
              <p style="margin:0;font-size:15px;line-height:1.7;color:#475569;">{{mensaje}}</p>
            </td>
          </tr>
          <tr>
            <td style="padding:16px 32px 10px;">
              <table role="presentation" width="100%" cellpadding="0" cellspacing="0" style="background-color:#f8fafc;border:1px solid #e2e8f0;border-radius:18px;">
                <tr>
                  <td style="padding:22px 22px 18px;">
                    <div style="margin:0 0 14px;font-size:11px;letter-spacing:1.1px;text-transform:uppercase;color:#0F4C81;font-weight:700;">{{badgeText}}</div>
                    <table role="presentation" width="100%" cellpadding="0" cellspacing="0">
                      <tr>
                        <td style="padding:8px 0;font-size:13px;color:#64748b;width:120px;">Código</td>
                        <td style="padding:8px 0;font-size:14px;font-weight:700;color:#0f172a;">{{codigo}}</td>
                      </tr>
                      <tr><td colspan="2" style="border-top:1px solid #e2e8f0;"></td></tr>
                      <tr>
                        <td style="padding:8px 0;font-size:13px;color:#64748b;">Ambiente</td>
                        <td style="padding:8px 0;font-size:14px;font-weight:600;color:#0f172a;">{{ambiente}}</td>
                      </tr>
                      <tr><td colspan="2" style="border-top:1px solid #e2e8f0;"></td></tr>
                      <tr>
                        <td style="padding:8px 0;font-size:13px;color:#64748b;">Fecha</td>
                        <td style="padding:8px 0;font-size:14px;font-weight:600;color:#0f172a;">{{fecha}}</td>
                      </tr>
                      <tr><td colspan="2" style="border-top:1px solid #e2e8f0;"></td></tr>
                      <tr>
                        <td style="padding:8px 0;font-size:13px;color:#64748b;">Horario</td>
                        <td style="padding:8px 0;font-size:14px;font-weight:600;color:#0f172a;">{{horario}}</td>
                      </tr>
                      <tr><td colspan="2" style="border-top:1px solid #e2e8f0;"></td></tr>
                      <tr>
                        <td style="padding:8px 0;font-size:13px;color:#64748b;">Evento</td>
                        <td style="padding:8px 0;font-size:14px;font-weight:600;color:#0f172a;">{{tipoEvento}}</td>
                      </tr>
{{additionalRows}}
                    </table>
                  </td>
                </tr>
              </table>
            </td>
          </tr>
          <tr>
            <td style="padding:16px 32px 0;">
              <table role="presentation" width="100%" cellpadding="0" cellspacing="0" style="background:linear-gradient(135deg,{{headerColor}} 0%,#1D6AA8 100%);border-radius:16px;">
                <tr>
                  <td bgcolor="{{headerColor}}" style="background-color:{{headerColor}};padding:20px 22px;">
                    <table role="presentation" width="100%" cellpadding="0" cellspacing="0">
                      <tr>
                        <td style="font-size:12px;color:rgba(255,255,255,0.78);text-transform:uppercase;letter-spacing:0.8px;font-weight:700;">Monto reservado</td>
                        <td style="text-align:right;font-size:24px;font-weight:800;color:#ffffff;">{{total}}</td>
                      </tr>
                      <tr>
                        <td style="padding-top:10px;font-size:12px;color:rgba(255,255,255,0.78);text-transform:uppercase;letter-spacing:0.8px;font-weight:700;">Estado</td>
                        <td style="text-align:right;padding-top:10px;">
                          <span style="display:inline-block;background-color:{{stateBadgeColor}};color:#ffffff;font-size:12px;font-weight:700;padding:6px 12px;border-radius:999px;">{{estado}}</span>
                        </td>
                      </tr>
                    </table>
                  </td>
                </tr>
              </table>
            </td>
          </tr>
          <tr>
            <td style="padding:24px 32px 8px;text-align:center;">
              <p style="margin:0 0 14px;font-size:14px;line-height:1.6;color:#64748b;">{{actionText}}</p>
              <a href="https://wa.me/{{celular}}?text=Hola!%20Tengo%20una%20consulta%20sobre%20mi%20reserva%20{{codigo}}" style="display:inline-block;background-color:#25d366;color:#ffffff;text-decoration:none;padding:13px 24px;border-radius:999px;font-size:14px;font-weight:700;">💬 Contactar por WhatsApp</a>
            </td>
          </tr>
          <tr>
            <td style="padding:24px 32px 28px;text-align:center;border-top:1px solid #e2e8f0;">
              <p style="margin:0 0 4px;font-size:13px;font-weight:700;color:#0f172a;">Administración · Centro Recreacional Campestre José Antonio</p>
              <p style="margin:0;font-size:12px;color:#94a3b8;">{{footerText}}</p>
              <p style="margin:6px 0 0;font-size:12px;color:#94a3b8;">Este mensaje se genera automáticamente. Por favor, no respondas a este correo.</p>
            </td>
          </tr>
        </table>
      </td>
    </tr>
  </table>
</body>
</html>
""";

        return template
                .replace("{{nombre}}", nombre)
                .replace("{{codigo}}", codigo)
                .replace("{{ambiente}}", ambiente)
                .replace("{{fecha}}", fecha)
                .replace("{{horario}}", horario)
                .replace("{{total}}", total)
                .replace("{{estado}}", estado)
                .replace("{{celular}}", celular)
                .replace("{{tipoEvento}}", tipoEvento)
                .replace("{{additionalRows}}", additionalRows)
                .replace("{{subtitulo}}", subtitulo)
                .replace("{{mensaje}}", mensaje)
                .replace("{{badgeText}}", badgeText)
                .replace("{{headerColor}}", headerColor)
                .replace("{{stateBadgeColor}}", stateBadgeColor)
                .replace("{{footerText}}", footerText)
                .replace("{{actionText}}", actionText)
                .replace("{{icon}}", icon);
    }

    private String buildWhatsAppMessage(Reservation r) {
        return String.format(
                "Hola %s! Tu reserva %s est\u00e1 confirmada.\n\n" +
                "Ambiente: %s\n" +
                "Fecha: %s\n" +
                "Total: S/ %.2f\n" +
                "Estado: %s\n\n" +
                "Gracias por preferirnos!",
                r.getClient().getNombre(),
                r.getCodigoReserva(),
                r.getEnvironment().getNombre(),
                r.getFechaEvento(),
                r.getPrecioTotal(),
                r.getEstado().name()
        );
    }

    private NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .reservationId(n.getReservation().getId())
                .tipo(n.getTipo().name())
                .destinatario(n.getDestinatario())
                .asunto(n.getAsunto())
                .mensaje(n.getMensaje())
                .estadoEnvio(n.getEstadoEnvio().name())
                .fechaEnvio(n.getFechaEnvio())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
