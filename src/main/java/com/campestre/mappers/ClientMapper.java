package com.campestre.mappers;

import com.campestre.dto.response.*;
import com.campestre.entities.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ClientMapper {

    public ClientResponse toResponse(Client client) {
        long totalReservas = client.getReservations() != null ? client.getReservations().size() : 0;

        List<ReservationSummaryResponse> ultimasReservas = List.of();
        if (client.getReservations() != null && !client.getReservations().isEmpty()) {
            ultimasReservas = client.getReservations().stream()
                    .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                    .limit(5)
                    .map(this::toSummary)
                    .collect(Collectors.toList());
        }

        return ClientResponse.builder()
                .id(client.getId())
                .nombre(client.getNombre())
                .celular(client.getCelular())
                .email(client.getEmail())
                .documentoIdentidad(client.getDocumentoIdentidad())
                .direccion(client.getDireccion())
                .createdAt(client.getCreatedAt())
                .totalReservas(totalReservas)
                .ultimasReservas(ultimasReservas)
                .build();
    }

    private ReservationSummaryResponse toSummary(Reservation r) {
        String estadoPago = "PENDIENTE";
        if (r.getPayments() != null && !r.getPayments().isEmpty()) {
            estadoPago = "PARCIAL";
        }

        return ReservationSummaryResponse.builder()
                .id(r.getId())
                .codigoReserva(r.getCodigoReserva())
                .ambienteNombre(r.getEnvironment().getNombre())
                .fechaEvento(r.getFechaEvento())
                .estado(r.getEstado().name())
                .precioTotal(r.getPrecioTotal())
                .estadoPago(estadoPago)
                .build();
    }
}
