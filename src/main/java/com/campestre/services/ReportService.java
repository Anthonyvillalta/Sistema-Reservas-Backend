package com.campestre.services;

import com.campestre.dto.response.ReportResponse;
import com.campestre.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ReportService {

    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;
    private final ClientRepository clientRepository;
    private final EnvironmentRepository environmentRepository;

    public ReportService(ReservationRepository reservationRepository,
                         PaymentRepository paymentRepository,
                         ClientRepository clientRepository,
                         EnvironmentRepository environmentRepository) {
        this.reservationRepository = reservationRepository;
        this.paymentRepository = paymentRepository;
        this.clientRepository = clientRepository;
        this.environmentRepository = environmentRepository;
    }

    public ReportResponse ingresosMensuales(int anio, int mes) {
        LocalDateTime inicio = LocalDateTime.of(anio, mes, 1, 0, 0);
        LocalDateTime fin = inicio.plusMonths(1).minusSeconds(1);

        BigDecimal ingresos = paymentRepository.sumIngresosByRango(inicio, fin);
        long totalReservas = reservationRepository.countActiveReservationsInRange(inicio, fin);

        Map<String, Object> data = new HashMap<>();
        data.put("anio", anio);
        data.put("mes", mes);
        data.put("ingresosTotales", ingresos);
        data.put("totalReservas", totalReservas);
        data.put("promedioPorReserva", totalReservas > 0 ?
                ingresos.divide(BigDecimal.valueOf(totalReservas), 2, java.math.RoundingMode.HALF_UP) :
                BigDecimal.ZERO);

        return ReportResponse.builder().tipo("INGRESOS_MENSUALES").datos(data).build();
    }

    public ReportResponse ocupacion(LocalDate fechaInicio, LocalDate fechaFin) {
        LocalDateTime inicio = fechaInicio.atStartOfDay();
        LocalDateTime fin = fechaFin.atTime(LocalTime.MAX);

        long totalReservas = reservationRepository.countActiveReservationsInRange(inicio, fin);
        long totalAmbientes = environmentRepository.count();

        Map<String, Object> data = new HashMap<>();
        data.put("fechaInicio", fechaInicio.toString());
        data.put("fechaFin", fechaFin.toString());
        data.put("totalReservas", totalReservas);
        data.put("totalAmbientes", totalAmbientes);

        return ReportResponse.builder().tipo("OCUPACION").datos(data).build();
    }

    public ReportResponse reservasPorRango(LocalDate fechaInicio, LocalDate fechaFin) {
        LocalDateTime inicio = fechaInicio.atStartOfDay();
        LocalDateTime fin = fechaFin.atTime(LocalTime.MAX);

        var reservas = reservationRepository.findByFechaInicioBetweenOrderByFechaInicio(inicio, fin);

        Map<String, Object> data = new HashMap<>();
        data.put("fechaInicio", fechaInicio.toString());
        data.put("fechaFin", fechaFin.toString());
        data.put("totalReservas", reservas.size());
        data.put("reservas", reservas.stream().map(r -> Map.of(
                "codigo", r.getCodigoReserva(),
                "cliente", r.getClient().getNombre(),
                "ambiente", r.getEnvironment().getNombre(),
                "fecha", r.getFechaEvento().toString(),
                "estado", r.getEstado().name(),
                "precioTotal", r.getPrecioTotal()
        )).collect(Collectors.toList()));

        return ReportResponse.builder().tipo("RESERVAS_RANGO").datos(data).build();
    }

    public ReportResponse clientesFrecuentes(int limite) {
        var clientes = clientRepository.findAll().stream()
                .map(c -> {
                    long totalReservas = c.getReservations() != null ? c.getReservations().size() : 0;
                    return Map.of(
                            "id", c.getId(),
                            "nombre", c.getNombre(),
                            "celular", c.getCelular(),
                            "email", c.getEmail() != null ? c.getEmail() : "",
                            "totalReservas", totalReservas
                    );
                })
                .sorted((a, b) -> Long.compare((Long) b.get("totalReservas"), (Long) a.get("totalReservas")))
                .limit(limite)
                .collect(Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        data.put("limite", limite);
        data.put("clientes", clientes);

        return ReportResponse.builder().tipo("CLIENTES_FRECUENTES").datos(data).build();
    }

    public ReportResponse pagosParcialesVsCompletos(LocalDate fechaInicio, LocalDate fechaFin) {
        LocalDateTime inicio = fechaInicio.atStartOfDay();
        LocalDateTime fin = fechaFin.atTime(LocalTime.MAX);

        long parcialesCount = paymentRepository.countPagosParcialesByRango(inicio, fin);
        long completosCount = paymentRepository.countPagosCompletosByRango(inicio, fin);

        Map<String, Object> data = new HashMap<>();
        data.put("fechaInicio", fechaInicio.toString());
        data.put("fechaFin", fechaFin.toString());
        data.put("pagosParciales", parcialesCount);
        data.put("pagosCompletos", completosCount);
        data.put("total", parcialesCount + completosCount);

        return ReportResponse.builder().tipo("PAGOS_PARCIALES_VS_COMPLETOS").datos(data).build();
    }
}
