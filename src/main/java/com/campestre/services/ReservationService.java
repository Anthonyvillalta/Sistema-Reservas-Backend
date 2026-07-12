package com.campestre.services;

import com.campestre.dto.request.ReservationRequest;
import com.campestre.dto.response.DisponibilidadResponse;
import com.campestre.dto.response.ReservationResponse;
import com.campestre.entities.*;
import com.campestre.enums.*;
import com.campestre.exceptions.BusinessException;
import com.campestre.exceptions.ResourceNotFoundException;
import com.campestre.mappers.ReservationMapper;
import com.campestre.repositories.*;
import com.campestre.utils.ReservationCodeGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReservationService {

    private static final Logger log = LoggerFactory.getLogger(ReservationService.class);

    private final ReservationRepository reservationRepository;
    private final ClientRepository clientRepository;
    private final EnvironmentRepository environmentRepository;
    private final MaintenanceRepository maintenanceRepository;
    private final ReservationMapper reservationMapper;
    private final NotificationService notificationService;

    public ReservationService(ReservationRepository reservationRepository,
                              ClientRepository clientRepository,
                              EnvironmentRepository environmentRepository,
                              MaintenanceRepository maintenanceRepository,
                              ReservationMapper reservationMapper,
                              NotificationService notificationService) {
        this.reservationRepository = reservationRepository;
        this.clientRepository = clientRepository;
        this.environmentRepository = environmentRepository;
        this.maintenanceRepository = maintenanceRepository;
        this.reservationMapper = reservationMapper;
        this.notificationService = notificationService;
    }

    @Transactional
    public List<ReservationResponse> findAll(String estado, Long environmentId, Long clientId,
                                              LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        // Auto advance CONFIRMADO → EN_PROCESO for any past-due reservations
        autoUpdateAllPending();
        ReservationStatus status = estado != null ? ReservationStatus.valueOf(estado.toUpperCase()) : null;
        List<Reservation> reservations = reservationRepository.searchReservations(
                status, environmentId, clientId, fechaInicio, fechaFin);
        return reservations.stream()
                .map(reservationMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void autoUpdateAllPending() {
        LocalDateTime now = LocalDateTime.now();

        // CONFIRMADO → EN_PROCESO
        List<Reservation> toStart = reservationRepository
                .findByEstadoAndFechaInicioBefore(ReservationStatus.CONFIRMADO, now);
        for (Reservation r : toStart) {
            r.setEstado(ReservationStatus.EN_PROCESO);
            reservationRepository.save(r);
            log.info("Auto-actualizada a EN_PROCESO: {}", r.getCodigoReserva());
        }

        // EN_PROCESO → FINALIZADO
        List<Reservation> inProcess = reservationRepository
                .findByEstadoAndFechaFinBefore(ReservationStatus.EN_PROCESO, now);
        for (Reservation r : inProcess) {
            r.setEstado(ReservationStatus.FINALIZADO);
            reservationRepository.save(r);
            log.info("Auto-actualizada a FINALIZADO: {}", r.getCodigoReserva());
        }
    }

    @Transactional(readOnly = true)
    public ReservationResponse findById(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva", id));
        autoUpdateStatus(reservation);
        return reservationMapper.toResponse(reservation);
    }

    /**
     * Auto-advances reservation status based on current time:
     * CONFIRMADO → EN_PROCESO when the event start time arrives
     * EN_PROCESO → FINALIZADO when the event end time passes
     */
    private void autoUpdateStatus(Reservation reservation) {
        LocalDateTime now = LocalDateTime.now();
        boolean started = reservation.getFechaInicio() != null && now.isAfter(reservation.getFechaInicio());
        boolean ended = reservation.getFechaFin() != null && now.isAfter(reservation.getFechaFin());

        if (ended && reservation.getEstado() == ReservationStatus.EN_PROCESO) {
            reservation.setEstado(ReservationStatus.FINALIZADO);
            reservationRepository.save(reservation);
            log.info("Auto-actualizada a FINALIZADO: {}", reservation.getCodigoReserva());
        } else if (started && reservation.getEstado() == ReservationStatus.CONFIRMADO) {
            reservation.setEstado(ReservationStatus.EN_PROCESO);
            reservationRepository.save(reservation);
            log.info("Auto-actualizada a EN_PROCESO: {}", reservation.getCodigoReserva());
        }
    }

    @Transactional
    public ReservationResponse create(ReservationRequest request, User user) {
        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", request.getClientId()));

        Environment environment = environmentRepository.findById(request.getEnvironmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Ambiente", request.getEnvironmentId()));

        if (environment.getEstado() == EnvironmentStatus.MANTENIMIENTO) {
            throw new BusinessException("SOLAPAMIENTO",
                    "El ambiente '" + environment.getNombre() + "' está en mantenimiento y no puede reservarse");
        }

        LocalDateTime fechaInicio;
        LocalDateTime fechaFin;

        if (environment.getTipo() == EnvironmentType.HORAS) {
            if (request.getHoraInicio() == null || request.getHoraFin() == null) {
                throw new BusinessException("Para ambientes por horas debe especificar hora de inicio y fin");
            }
            fechaInicio = LocalDateTime.of(request.getFechaEvento(), request.getHoraInicio());
            fechaFin = LocalDateTime.of(request.getFechaEvento(), request.getHoraFin());
        } else if (request.getHoraInicio() != null && request.getHoraFin() != null) {
            // EVENTO con horario específico
            fechaInicio = LocalDateTime.of(request.getFechaEvento(), request.getHoraInicio());
            fechaFin = LocalDateTime.of(request.getFechaEvento(), request.getHoraFin());
        } else {
            // EVENTO sin horario (día completo)
            fechaInicio = request.getFechaEvento().atStartOfDay();
            fechaFin = request.getFechaEvento().atTime(LocalTime.MAX);
        }

        // Always validate that the event date is not in the past
        if (request.getFechaEvento().isBefore(LocalDate.now())) {
            throw new BusinessException("No se pueden crear reservas en el pasado");
        }

        // Only validate past time for HORAS or EVENTO with specific hours
        if (environment.getTipo() == EnvironmentType.HORAS || request.getHoraInicio() != null) {
            if (fechaInicio.isBefore(LocalDateTime.now())) {
                throw new BusinessException("No se pueden crear reservas en el pasado");
            }
        }

        boolean enMantenimiento = maintenanceRepository.existsOverlappingMaintenance(environment.getId(), fechaInicio, fechaFin);
        if (enMantenimiento) {
            throw new BusinessException("SOLAPAMIENTO",
                    "El ambiente '" + environment.getNombre() + "' está en mantenimiento en esa fecha");
        }

        boolean solapada = reservationRepository.existsOverlappingReservation(environment.getId(), fechaInicio, fechaFin, null);
        if (solapada) {
            throw new BusinessException("SOLAPAMIENTO",
                    "Ya existe una reserva para '" + environment.getNombre() + "' en esa fecha y horario");
        }

        long count = reservationRepository.count();
        String codigo = ReservationCodeGenerator.generateCode(request.getFechaEvento(), count + 1);

        // El frontend envía el precioTotal ya calculado (precioBase × horas + adicionales)
        // Lo usamos directamente respetando el cálculo del cliente
        BigDecimal precioTotal = request.getPrecioTotal();

        Reservation reservation = Reservation.builder()
                .codigoReserva(codigo)
                .client(client)
                .environment(environment)
                .fechaEvento(request.getFechaEvento())
                .horaInicio(request.getHoraInicio())
                .horaFin(request.getHoraFin())
                .fechaInicio(fechaInicio)
                .fechaFin(fechaFin)
                .estado(ReservationStatus.RESERVADO)
                .precioTotal(precioTotal)
                .adelantoRequerido(request.getAdelantoRequerido() != null && request.getAdelantoRequerido())
                .notas(request.getNotas())
                .tipoEvento(request.getTipoEvento())
                .precioSillas(request.getPrecioSillas())
                .precioMotor(request.getPrecioMotor())
                .createdBy(user)
                .build();

        reservation = reservationRepository.save(reservation);
        log.info("Reserva creada: {} - Ambiente: {} - Cliente: {}",
                codigo, environment.getNombre(), client.getNombre());

        return reservationMapper.toResponse(reservation);
    }

    @Transactional
    public ReservationResponse update(Long id, ReservationRequest request) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva", id));

        if (reservation.getEstado() == ReservationStatus.CANCELADO ||
                reservation.getEstado() == ReservationStatus.FINALIZADO) {
            throw new BusinessException("No se puede modificar una reserva " + reservation.getEstado().name());
        }

        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", request.getClientId()));

        Environment environment = reservation.getEnvironment();

        LocalDateTime fechaInicio;
        LocalDateTime fechaFin;

        if (environment.getTipo() == EnvironmentType.HORAS) {
            fechaInicio = LocalDateTime.of(request.getFechaEvento(), request.getHoraInicio());
            fechaFin = LocalDateTime.of(request.getFechaEvento(), request.getHoraFin());
        } else {
            fechaInicio = request.getFechaEvento().atStartOfDay();
            fechaFin = request.getFechaEvento().atTime(LocalTime.MAX);
        }

        boolean solapada = reservationRepository.existsOverlappingReservation(
                environment.getId(), fechaInicio, fechaFin, id);
        if (solapada) {
            throw new BusinessException("SOLAPAMIENTO",
                    "Ya existe otra reserva en esa fecha y horario");
        }

        reservation.setClient(client);
        reservation.setFechaEvento(request.getFechaEvento());
        reservation.setHoraInicio(request.getHoraInicio());
        reservation.setHoraFin(request.getHoraFin());
        reservation.setFechaInicio(fechaInicio);
        reservation.setFechaFin(fechaFin);
        reservation.setPrecioTotal(request.getPrecioTotal());
        reservation.setAdelantoRequerido(request.getAdelantoRequerido() != null && request.getAdelantoRequerido());
        reservation.setNotas(request.getNotas());

        reservation = reservationRepository.save(reservation);
        log.info("Reserva actualizada: {}", reservation.getCodigoReserva());

        return reservationMapper.toResponse(reservation);
    }

    @Transactional
    public ReservationResponse updateStatus(Long id, String nuevoEstado) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva", id));

        ReservationStatus status;
        try {
            status = ReservationStatus.valueOf(nuevoEstado.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Estado inválido: " + nuevoEstado);
        }

        if (reservation.getEstado() == ReservationStatus.CANCELADO &&
                status != ReservationStatus.CANCELADO) {
            throw new BusinessException("No se puede reactivar una reserva cancelada");
        }

        boolean cambioConfirmacion = status == ReservationStatus.CONFIRMADO && reservation.getEstado() != ReservationStatus.CONFIRMADO;
        boolean cambioCancelacion = status == ReservationStatus.CANCELADO && reservation.getEstado() != ReservationStatus.CANCELADO;

        reservation.setEstado(status);
        reservation = reservationRepository.save(reservation);
        log.info("Estado de reserva {} actualizado a: {}", reservation.getCodigoReserva(), status);

        if (cambioConfirmacion) {
            notificationService.sendEmailConfirmation(reservation.getId());
        }

        if (cambioCancelacion) {
            notificationService.sendCancellationEmail(reservation.getId());
        }

        return reservationMapper.toResponse(reservation);
    }

    @Transactional
    public void cancel(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva", id));

        if (reservation.getEstado() == ReservationStatus.CANCELADO) {
            throw new BusinessException("La reserva ya está cancelada");
        }

        if (reservation.getEstado() == ReservationStatus.FINALIZADO) {
            throw new BusinessException("No se puede cancelar una reserva finalizada");
        }

        reservation.setEstado(ReservationStatus.CANCELADO);
        reservationRepository.save(reservation);
        notificationService.sendCancellationEmail(reservation.getId());
        log.info("Reserva cancelada: {}", reservation.getCodigoReserva());
    }

    @Transactional(readOnly = true)
    public DisponibilidadResponse checkDisponibilidad(Long environmentId, LocalDate fecha) {
        Environment environment = environmentRepository.findById(environmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Ambiente", environmentId));

        LocalDateTime inicio = fecha.atStartOfDay();
        LocalDateTime fin = fecha.atTime(LocalTime.MAX);

        boolean enMantenimiento = maintenanceRepository.existsOverlappingMaintenance(environmentId, inicio, fin);

        if (enMantenimiento) {
            return DisponibilidadResponse.builder()
                    .environmentId(environmentId)
                    .ambienteNombre(environment.getNombre())
                    .fecha(fecha.toString())
                    .disponible(false)
                    .mensaje("El ambiente está en mantenimiento")
                    .horariosDisponibles(List.of())
                    .horariosOcupados(List.of())
                    .build();
        }

        List<Reservation> reservas = reservationRepository.findActiveReservationsInRange(environmentId, inicio, fin);
        List<String> horariosOcupados = new ArrayList<>();

        // Check if any reservation has specific hours — if so, show hourly occupancy
        boolean algunaConHoras = reservas.stream().anyMatch(r -> r.getHoraInicio() != null && r.getHoraFin() != null);

        if (algunaConHoras) {
            for (Reservation r : reservas) {
                if (r.getHoraInicio() != null && r.getHoraFin() != null) {
                    horariosOcupados.add(r.getHoraInicio() + " - " + r.getHoraFin());
                }
            }
        } else {
            if (!reservas.isEmpty()) {
                return DisponibilidadResponse.builder()
                        .environmentId(environmentId)
                        .ambienteNombre(environment.getNombre())
                        .fecha(fecha.toString())
                        .disponible(false)
                        .mensaje("El ambiente ya está reservado para esta fecha")
                        .horariosDisponibles(List.of())
                        .horariosOcupados(List.of("Todo el día"))
                        .build();
            }
        }

        List<String> horariosDisponibles = List.of();
        if (environment.getTipo() == EnvironmentType.HORAS) {
            horariosDisponibles = generateHorariosDisponibles(horariosOcupados);
        }

        boolean disponible = reservas.isEmpty() && !enMantenimiento;

        return DisponibilidadResponse.builder()
                .environmentId(environmentId)
                .ambienteNombre(environment.getNombre())
                .fecha(fecha.toString())
                .disponible(disponible)
                .mensaje(disponible ? "Ambiente disponible" : "Ambiente ocupado")
                .horariosDisponibles(horariosDisponibles)
                .horariosOcupados(horariosOcupados)
                .build();
    }

    private List<String> generateHorariosDisponibles(List<String> ocupados) {
        List<String> horarios = new ArrayList<>();
        for (int h = 8; h < 22; h++) {
            String slot = String.format("%02d:00 - %02d:00", h, h + 1);
            if (!ocupados.contains(slot)) {
                horarios.add(slot);
            }
        }
        return horarios;
    }
}
