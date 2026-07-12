package com.campestre.services;

import com.campestre.dto.request.PaymentRequest;
import com.campestre.dto.response.PaymentResponse;
import com.campestre.entities.Payment;
import com.campestre.entities.Reservation;
import com.campestre.enums.*;
import com.campestre.exceptions.BusinessException;
import com.campestre.exceptions.ResourceNotFoundException;
import com.campestre.mappers.ReservationMapper;
import com.campestre.repositories.PaymentRepository;
import com.campestre.repositories.ReservationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationMapper reservationMapper;

    public PaymentService(PaymentRepository paymentRepository,
                          ReservationRepository reservationRepository,
                          ReservationMapper reservationMapper) {
        this.paymentRepository = paymentRepository;
        this.reservationRepository = reservationRepository;
        this.reservationMapper = reservationMapper;
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> findByReservation(Long reservationId) {
        return paymentRepository.findByReservationIdOrderByFechaPagoDesc(reservationId)
                .stream()
                .map(reservationMapper::toPaymentResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public PaymentResponse create(PaymentRequest request) {
        Reservation reservation = reservationRepository.findById(request.getReservationId())
                .orElseThrow(() -> new ResourceNotFoundException("Reserva", request.getReservationId()));

        if (reservation.getEstado() == ReservationStatus.CANCELADO) {
            throw new BusinessException("No se pueden registrar pagos en una reserva cancelada");
        }

        if (reservation.getEstado() == ReservationStatus.FINALIZADO) {
            throw new BusinessException("No se pueden registrar pagos en una reserva finalizada");
        }

        TipoPago tipoPago;
        MetodoPago metodoPago;
        try {
            tipoPago = TipoPago.valueOf(request.getTipoPago().toUpperCase());
            metodoPago = MetodoPago.valueOf(request.getMetodoPago().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Tipo de pago o método de pago inválido");
        }

        BigDecimal totalPagado = paymentRepository.sumPagosByReservation(reservation.getId());
        BigDecimal nuevoTotal = totalPagado.add(request.getMonto());

        if (nuevoTotal.compareTo(reservation.getPrecioTotal()) > 0) {
            throw new BusinessException("El monto total de pagos excede el precio de la reserva");
        }

        Payment payment = Payment.builder()
                .reservation(reservation)
                .monto(request.getMonto())
                .tipoPago(tipoPago)
                .metodoPago(metodoPago)
                .estado(PaymentStatus.PAGADO)
                .fechaPago(LocalDateTime.now())
                .referencia(request.getReferencia())
                .build();

        payment = paymentRepository.save(payment);
        log.info("Pago registrado: S/{} - Reserva: {} - Tipo: {}",
                request.getMonto(), reservation.getCodigoReserva(), tipoPago);

        return reservationMapper.toPaymentResponse(payment);
    }
}
