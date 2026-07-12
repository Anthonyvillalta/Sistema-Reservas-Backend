package com.campestre.repositories;

import com.campestre.entities.Payment;
import com.campestre.entities.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByReservationIdOrderByFechaPagoDesc(Long reservationId);

    @Query("SELECT COALESCE(SUM(p.monto), 0) FROM Payment p WHERE p.reservation.id = :reservationId AND p.estado = 'PAGADO'")
    BigDecimal sumPagosByReservation(@Param("reservationId") Long reservationId);

    @Query("SELECT COALESCE(SUM(p.monto), 0) FROM Payment p WHERE " +
           "p.fechaPago >= :inicio AND p.fechaPago <= :fin AND p.estado = 'PAGADO'")
    BigDecimal sumIngresosByRango(@Param("inicio") LocalDateTime inicio,
                                   @Param("fin") LocalDateTime fin);

    @Query("SELECT COUNT(p) FROM Payment p WHERE p.tipoPago = 'ADELANTO' AND " +
           "p.fechaPago >= :inicio AND p.fechaPago <= :fin")
    long countPagosParcialesByRango(@Param("inicio") LocalDateTime inicio,
                                     @Param("fin") LocalDateTime fin);

    @Query("SELECT COUNT(p) FROM Payment p WHERE p.tipoPago = 'COMPLETO' AND " +
           "p.fechaPago >= :inicio AND p.fechaPago <= :fin")
    long countPagosCompletosByRango(@Param("inicio") LocalDateTime inicio,
                                     @Param("fin") LocalDateTime fin);
}
