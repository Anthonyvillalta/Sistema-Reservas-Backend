package com.campestre.repositories;

import com.campestre.entities.Maintenance;
import com.campestre.enums.MaintenanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MaintenanceRepository extends JpaRepository<Maintenance, Long> {

    List<Maintenance> findByEnvironmentIdOrderByFechaInicioDesc(Long environmentId);

    @Query("SELECT COUNT(m) > 0 FROM Maintenance m " +
           "WHERE m.environment.id = :environmentId " +
           "AND m.estado = 'MANTENIMIENTO' " +
           "AND m.fechaInicio < :fechaFin AND m.fechaFin > :fechaInicio")
    boolean existsOverlappingMaintenance(
            @Param("environmentId") Long environmentId,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin);

    List<Maintenance> findByEstado(MaintenanceStatus estado);

    @Query("SELECT m FROM Maintenance m WHERE " +
           "m.fechaInicio >= :inicio AND m.fechaFin <= :fin " +
           "AND (:environmentId IS NULL OR m.environment.id = :environmentId) " +
           "ORDER BY m.fechaInicio")
    List<Maintenance> findMaintenancesInRange(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin,
            @Param("environmentId") Long environmentId);

    @Query("SELECT m FROM Maintenance m WHERE m.estado = :estado AND m.fechaInicio <= :now")
    List<Maintenance> findByEstadoAndFechaInicioBefore(
            @Param("estado") MaintenanceStatus estado,
            @Param("now") LocalDateTime now);

    @Query("SELECT m FROM Maintenance m WHERE m.estado = :estado AND m.fechaFin <= :now")
    List<Maintenance> findByEstadoAndFechaFinBefore(
            @Param("estado") MaintenanceStatus estado,
            @Param("now") LocalDateTime now);
}
