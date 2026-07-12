package com.campestre.mappers;

import com.campestre.dto.response.PaymentResponse;
import com.campestre.dto.response.ReservationResponse;
import com.campestre.entities.Payment;
import com.campestre.entities.Reservation;
import com.campestre.enums.PaymentStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ReservationMapper {

    public ReservationResponse toResponse(Reservation r) {
        BigDecimal totalPagado = BigDecimal.ZERO;
        if (r.getPayments() != null && !r.getPayments().isEmpty()) {
            totalPagado = r.getPayments().stream()
                    .filter(p -> p.getEstado() == PaymentStatus.PAGADO)
                    .map(Payment::getMonto)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        BigDecimal saldoPendiente = r.getPrecioTotal().subtract(totalPagado);
        String estadoPago = totalPagado.compareTo(BigDecimal.ZERO) == 0 ? "PENDIENTE" :
                totalPagado.compareTo(r.getPrecioTotal()) >= 0 ? "PAGADO" : "PARCIAL";

        List<com.campestre.dto.response.PaymentResponse> pagos = List.of();
        if (r.getPayments() != null && !r.getPayments().isEmpty()) {
            pagos = r.getPayments().stream()
                    .map(this::toPaymentResponse)
                    .collect(Collectors.toList());
        }

        return ReservationResponse.builder()
                .id(r.getId())
                .codigoReserva(r.getCodigoReserva())
                .clientId(r.getClient().getId())
                .clienteNombre(r.getClient().getNombre())
                .clienteCelular(r.getClient().getCelular())
                .clienteEmail(r.getClient().getEmail())
                .environmentId(r.getEnvironment().getId())
                .ambienteNombre(r.getEnvironment().getNombre())
                .ambienteTipo(r.getEnvironment().getTipo().name())
                .fechaEvento(r.getFechaEvento())
                .horaInicio(r.getHoraInicio())
                .horaFin(r.getHoraFin())
                .fechaInicio(r.getFechaInicio())
                .fechaFin(r.getFechaFin())
                .estado(r.getEstado().name())
                .precioTotal(r.getPrecioTotal())
                .totalPagado(totalPagado)
                .saldoPendiente(saldoPendiente)
                .estadoPago(estadoPago)
                .adelantoRequerido(r.getAdelantoRequerido())
                .notas(r.getNotas())
                .tipoEvento(r.getTipoEvento())
                .precioSillas(r.getPrecioSillas())
                .precioMotor(r.getPrecioMotor())
                .createdBy(r.getCreatedBy().getId())
                .creadoPorNombre(r.getCreatedBy().getNombreCompleto())
                .createdAt(r.getCreatedAt())
                .pagos(pagos)
                .build();
    }

    public PaymentResponse toPaymentResponse(Payment p) {
        return PaymentResponse.builder()
                .id(p.getId())
                .reservationId(p.getReservation().getId())
                .codigoReserva(p.getReservation().getCodigoReserva())
                .monto(p.getMonto())
                .tipoPago(p.getTipoPago().name())
                .metodoPago(p.getMetodoPago().name())
                .estado(p.getEstado().name())
                .fechaPago(p.getFechaPago())
                .referencia(p.getReferencia())
                .createdAt(p.getCreatedAt())
                .build();
    }
}
