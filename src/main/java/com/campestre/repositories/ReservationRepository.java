package com.campestre.repositories;

import com.campestre.entities.Reservation;
import com.campestre.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByClientIdOrderByFechaInicioDesc(Long clientId);

    List<Reservation> findByEnvironmentIdOrderByFechaInicioDesc(Long environmentId);

    @Query("SELECT COUNT(r) > 0 FROM Reservation r " +
           "WHERE r.environment.id = :environmentId " +
           "AND r.estado NOT IN ('CANCELADO', 'FINALIZADO') " +
           "AND r.fechaInicio < :fechaFin AND r.fechaFin > :fechaInicio " +
           "AND (:excludeId IS NULL OR r.id != :excludeId)")
    boolean existsOverlappingReservation(
            @Param("environmentId") Long environmentId,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin,
            @Param("excludeId") Long excludeId);

    @Query("SELECT r FROM Reservation r WHERE " +
           "(:estado IS NULL OR r.estado = :estado) AND " +
           "(:environmentId IS NULL OR r.environment.id = :environmentId) AND " +
           "(:clientId IS NULL OR r.client.id = :clientId) AND " +
           "(:fechaInicio IS NULL OR r.fechaInicio >= :fechaInicio) AND " +
           "(:fechaFin IS NULL OR r.fechaFin <= :fechaFin) " +
           "ORDER BY r.fechaInicio ASC")
    List<Reservation> searchReservations(
            @Param("estado") ReservationStatus estado,
            @Param("environmentId") Long environmentId,
            @Param("clientId") Long clientId,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin);

    @Query("SELECT r FROM Reservation r WHERE " +
           "r.environment.id = :environmentId " +
           "AND r.fechaInicio >= :fechaInicio AND r.fechaFin <= :fechaFin " +
           "AND r.estado NOT IN ('CANCELADO', 'FINALIZADO') " +
           "ORDER BY r.fechaInicio")
    List<Reservation> findActiveReservationsInRange(
            @Param("environmentId") Long environmentId,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin);

    List<Reservation> findByFechaInicioBetweenOrderByFechaInicio(
            LocalDateTime inicio, LocalDateTime fin);

    long countByEstado(ReservationStatus estado);

    @Query("SELECT COUNT(r) FROM Reservation r WHERE " +
           "r.fechaInicio >= :inicio AND r.fechaInicio <= :fin AND " +
           "r.estado NOT IN ('CANCELADO')")
    long countActiveReservationsInRange(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin);

    @Query("SELECT r FROM Reservation r WHERE r.estado = :estado AND r.fechaInicio <= :now")
    List<Reservation> findByEstadoAndFechaInicioBefore(
            @Param("estado") ReservationStatus estado,
            @Param("now") LocalDateTime now);

    @Query("SELECT r FROM Reservation r WHERE r.estado = :estado AND r.fechaFin <= :now")
    List<Reservation> findByEstadoAndFechaFinBefore(
            @Param("estado") ReservationStatus estado,
            @Param("now") LocalDateTime now);
}
