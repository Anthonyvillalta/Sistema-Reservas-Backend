package com.campestre.services;

import com.campestre.entities.Client;
import com.campestre.entities.Environment;
import com.campestre.entities.Reservation;
import com.campestre.enums.ReservationStatus;
import com.campestre.repositories.NotificationRepository;
import com.campestre.repositories.ReservationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSender;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class NotificationServiceTest {

    @Test
    void buildEmailHtmlShouldRenderHtmlWithoutFormatErrors() throws Exception {
        NotificationService service = new NotificationService(
                mock(NotificationRepository.class),
                mock(ReservationRepository.class),
                mock(JavaMailSender.class)
        );

        Reservation reservation = Reservation.builder()
                .codigoReserva("RES-001")
                .client(Client.builder()
                        .nombre("Juan Pérez")
                        .celular("999999999")
                        .email("juan@test.com")
                        .build())
                .environment(Environment.builder()
                        .nombre("Salón Principal")
                        .build())
                .fechaEvento(LocalDate.of(2026, 7, 8))
                .horaInicio(LocalTime.of(10, 0))
                .horaFin(LocalTime.of(12, 0))
                .precioTotal(new BigDecimal("150.50"))
                .estado(ReservationStatus.RESERVADO)
                .build();

        Method method = NotificationService.class.getDeclaredMethod("buildEmailHtml", Reservation.class);
        method.setAccessible(true);

        String html = (String) method.invoke(service, reservation);

        assertNotNull(html);
        assertTrue(html.contains("Juan Pérez"));
        assertTrue(html.contains("RES-001"));
        assertTrue(html.contains("Salón Principal"));
        assertTrue(html.contains("#0F4C81"));
        assertTrue(html.contains("100%"));
    }

    @Test
    void buildEmailHtmlShouldRenderCancellationTemplate() throws Exception {
        NotificationService service = new NotificationService(
                mock(NotificationRepository.class),
                mock(ReservationRepository.class),
                mock(JavaMailSender.class)
        );

        Reservation reservation = Reservation.builder()
                .codigoReserva("RES-002")
                .client(Client.builder()
                        .nombre("Ana López")
                        .celular("999999999")
                        .email("ana@test.com")
                        .build())
                .environment(Environment.builder()
                        .nombre("Salón Principal")
                        .build())
                .fechaEvento(LocalDate.of(2026, 7, 10))
                .precioTotal(new BigDecimal("80.00"))
                .estado(ReservationStatus.CANCELADO)
                .build();

        Method method = NotificationService.class.getDeclaredMethod("buildEmailHtml", Reservation.class, boolean.class);
        method.setAccessible(true);

        String html = (String) method.invoke(service, reservation, false);

        assertNotNull(html);
        assertTrue(html.contains("Reserva cancelada"));
        assertTrue(html.contains("RES-002"));
    }
}
