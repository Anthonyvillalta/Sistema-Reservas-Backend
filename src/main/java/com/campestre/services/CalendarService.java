package com.campestre.services;

import com.campestre.dto.response.CalendarEventResponse;
import com.campestre.entities.Maintenance;
import com.campestre.entities.Reservation;
import com.campestre.enums.ReservationStatus;
import com.campestre.repositories.EnvironmentRepository;
import com.campestre.repositories.MaintenanceRepository;
import com.campestre.repositories.ReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CalendarService {

    private final ReservationRepository reservationRepository;
    private final MaintenanceRepository maintenanceRepository;
    private final EnvironmentRepository environmentRepository;

    public CalendarService(ReservationRepository reservationRepository,
                           MaintenanceRepository maintenanceRepository,
                           EnvironmentRepository environmentRepository) {
        this.reservationRepository = reservationRepository;
        this.maintenanceRepository = maintenanceRepository;
        this.environmentRepository = environmentRepository;
    }

    @Transactional(readOnly = true)
    public List<CalendarEventResponse> getEvents(LocalDateTime fechaInicio, LocalDateTime fechaFin, Long ambienteId) {
        List<CalendarEventResponse> events = new ArrayList<>();

        List<Long> environmentIds;
        if (ambienteId != null) {
            environmentIds = List.of(ambienteId);
        } else {
            environmentIds = environmentRepository.findAll().stream()
                    .map(e -> e.getId())
                    .collect(Collectors.toList());
        }

        for (Long envId : environmentIds) {
            List<Reservation> reservas = reservationRepository.findActiveReservationsInRange(
                    envId, fechaInicio, fechaFin);

            for (Reservation r : reservas) {
                String color = getColorForStatus(r.getEstado());
                events.add(CalendarEventResponse.builder()
                        .id(r.getId())
                        .titulo(r.getClient().getNombre() + " - " + r.getEnvironment().getNombre())
                        .tipo("RESERVA")
                        .inicio(r.getFechaInicio())
                        .fin(r.getFechaFin())
                        .color(color)
                        .estado(r.getEstado().name())
                        .codigoReserva(r.getCodigoReserva())
                        .ambienteNombre(r.getEnvironment().getNombre())
                        .clienteNombre(r.getClient().getNombre())
                        .editable(true)
                        .build());
            }

            List<Maintenance> maintenances = maintenanceRepository.findMaintenancesInRange(
                    fechaInicio, fechaFin, envId);

            for (Maintenance m : maintenances) {
                events.add(CalendarEventResponse.builder()
                        .id(m.getId())
                        .titulo("Mantenimiento: " + m.getEnvironment().getNombre())
                        .tipo("MANTENIMIENTO")
                        .inicio(m.getFechaInicio())
                        .fin(m.getFechaFin())
                        .color("#f44336")
                        .estado(m.getEstado().name())
                        .ambienteNombre(m.getEnvironment().getNombre())
                        .editable(false)
                        .build());
            }
        }

        return events;
    }

    private String getColorForStatus(ReservationStatus status) {
        return switch (status) {
            case RESERVADO -> "#2196F3";
            case CONFIRMADO -> "#4CAF50";
            case EN_PROCESO -> "#FF9800";
            case FINALIZADO -> "#9E9E9E";
            case CANCELADO -> "#F44336";
        };
    }
}
