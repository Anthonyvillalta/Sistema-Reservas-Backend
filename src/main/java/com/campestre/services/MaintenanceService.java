package com.campestre.services;

import com.campestre.dto.request.MaintenanceRequest;
import com.campestre.dto.response.MaintenanceResponse;
import com.campestre.entities.Environment;
import com.campestre.entities.Maintenance;
import com.campestre.entities.User;
import com.campestre.enums.EnvironmentStatus;
import com.campestre.enums.MaintenanceStatus;
import com.campestre.exceptions.BusinessException;
import com.campestre.exceptions.ResourceNotFoundException;
import com.campestre.repositories.EnvironmentRepository;
import com.campestre.repositories.MaintenanceRepository;
import com.campestre.repositories.ReservationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MaintenanceService {

    private static final Logger log = LoggerFactory.getLogger(MaintenanceService.class);

    private final MaintenanceRepository maintenanceRepository;
    private final EnvironmentRepository environmentRepository;
    private final ReservationRepository reservationRepository;

    public MaintenanceService(MaintenanceRepository maintenanceRepository,
                              EnvironmentRepository environmentRepository,
                              ReservationRepository reservationRepository) {
        this.maintenanceRepository = maintenanceRepository;
        this.environmentRepository = environmentRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public List<MaintenanceResponse> findAll(Long ambienteId, String estado) {
        // Auto-advance maintenance statuses
        autoUpdateMaintenanceStatus();

        List<Maintenance> list;
        if (ambienteId != null) {
            list = maintenanceRepository.findByEnvironmentIdOrderByFechaInicioDesc(ambienteId);
        } else if (estado != null) {
            list = maintenanceRepository.findByEstado(MaintenanceStatus.valueOf(estado.toUpperCase()));
        } else {
            list = maintenanceRepository.findAll();
        }

        return list.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void autoUpdateMaintenanceStatus() {
        LocalDateTime now = LocalDateTime.now();

        // PROGRAMADO → MANTENIMIENTO when start time arrives
        List<Maintenance> toStart = maintenanceRepository
                .findByEstadoAndFechaInicioBefore(MaintenanceStatus.PROGRAMADO, now);
        for (Maintenance m : toStart) {
            m.setEstado(MaintenanceStatus.MANTENIMIENTO);
            maintenanceRepository.save(m);
            Environment env = m.getEnvironment();
            env.setEstado(EnvironmentStatus.MANTENIMIENTO);
            environmentRepository.save(env);
            log.info("Auto-actualizado mantenimiento a MANTENIMIENTO: {} - {}", m.getId(), env.getNombre());
        }

        // MANTENIMIENTO → FINALIZADO when end time passes
        List<Maintenance> toEnd = maintenanceRepository
                .findByEstadoAndFechaFinBefore(MaintenanceStatus.MANTENIMIENTO, now);
        for (Maintenance m : toEnd) {
            m.setEstado(MaintenanceStatus.FINALIZADO);
            maintenanceRepository.save(m);
            Environment env = m.getEnvironment();
            // Check if there are no other active maintenances
            boolean hasOtherActive = maintenanceRepository
                    .findByEnvironmentIdOrderByFechaInicioDesc(env.getId())
                    .stream()
                    .anyMatch(a -> a.getEstado() == MaintenanceStatus.MANTENIMIENTO && !a.getId().equals(m.getId()));
            if (!hasOtherActive) {
                env.setEstado(EnvironmentStatus.ACTIVO);
                environmentRepository.save(env);
                log.info("Ambiente {} vuelve a ACTIVO tras finalizar mantenimiento", env.getNombre());
            }
            log.info("Auto-actualizado mantenimiento a FINALIZADO: {}", m.getId());
        }
    }

    @Transactional
    public MaintenanceResponse create(MaintenanceRequest request, User user) {
        Environment environment = environmentRepository.findById(request.getEnvironmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Ambiente", request.getEnvironmentId()));

        if (request.getFechaInicio().isBefore(java.time.LocalDateTime.now())) {
            throw new BusinessException("La fecha de mantenimiento no puede ser en el pasado");
        }

        if (maintenanceRepository.existsOverlappingMaintenance(
                environment.getId(), request.getFechaInicio(), request.getFechaFin())) {
            throw new BusinessException("Ya existe un mantenimiento programado para este ambiente en ese rango de fechas");
        }

        if (reservationRepository.existsOverlappingReservation(
                environment.getId(), request.getFechaInicio(), request.getFechaFin(), null)) {
            throw new BusinessException("No se puede programar mantenimiento porque hay reservas activas en ese rango");
        }

        MaintenanceStatus status;
        try {
            status = request.getEstado() != null ?
                    MaintenanceStatus.valueOf(request.getEstado().toUpperCase()) :
                    MaintenanceStatus.PROGRAMADO;
        } catch (IllegalArgumentException e) {
            status = MaintenanceStatus.PROGRAMADO;
        }

        Maintenance maintenance = Maintenance.builder()
                .environment(environment)
                .fechaInicio(request.getFechaInicio())
                .fechaFin(request.getFechaFin())
                .motivo(request.getMotivo())
                .estado(status)
                .createdBy(user)
                .build();

        maintenance = maintenanceRepository.save(maintenance);

        // Don't set environment to MANTENIMIENTO yet — autoUpdate will handle it when start time arrives
        log.info("Mantenimiento programado para ambiente: {} - Motivo: {}",
                environment.getNombre(), request.getMotivo());

        return toResponse(maintenance);
    }

    @Transactional
    public MaintenanceResponse update(Long id, MaintenanceRequest request) {
        Maintenance maintenance = maintenanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mantenimiento", id));

        maintenance.setFechaInicio(request.getFechaInicio());
        maintenance.setFechaFin(request.getFechaFin());
        maintenance.setMotivo(request.getMotivo());

        if (request.getEstado() != null) {
            maintenance.setEstado(MaintenanceStatus.valueOf(request.getEstado().toUpperCase()));
        }

        maintenance = maintenanceRepository.save(maintenance);
        log.info("Mantenimiento actualizado: {}", id);

        return toResponse(maintenance);
    }

    @Transactional(readOnly = true)
    public MaintenanceResponse findById(Long id) {
        Maintenance maintenance = maintenanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mantenimiento", id));
        autoUpdateMaintenanceStatus();
        return toResponse(maintenance);
    }

    @Transactional
    public void delete(Long id) {
        Maintenance maintenance = maintenanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mantenimiento", id));

        Environment environment = maintenance.getEnvironment();

        maintenanceRepository.delete(maintenance);

        boolean hasActiveMaintenance = maintenanceRepository
                .findByEnvironmentIdOrderByFechaInicioDesc(environment.getId())
                .stream()
                .anyMatch(m -> m.getEstado() == MaintenanceStatus.MANTENIMIENTO);

        if (!hasActiveMaintenance) {
            environment.setEstado(EnvironmentStatus.ACTIVO);
            environmentRepository.save(environment);
        }

        log.info("Mantenimiento eliminado: {}", id);
    }

    private MaintenanceResponse toResponse(Maintenance m) {
        return MaintenanceResponse.builder()
                .id(m.getId())
                .environmentId(m.getEnvironment().getId())
                .ambienteNombre(m.getEnvironment().getNombre())
                .fechaInicio(m.getFechaInicio())
                .fechaFin(m.getFechaFin())
                .motivo(m.getMotivo())
                .estado(m.getEstado().name())
                .creadoPor(m.getCreatedBy().getNombreCompleto())
                .createdAt(m.getCreatedAt())
                .build();
    }
}
