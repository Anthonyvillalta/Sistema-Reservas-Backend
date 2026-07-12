package com.campestre.controllers;

import com.campestre.dto.response.ReportResponse;
import com.campestre.services.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/reportes")
@PreAuthorize("hasRole('ADMIN')")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/ingresos-mensuales")
    public ResponseEntity<ReportResponse> ingresosMensuales(
            @RequestParam int anio,
            @RequestParam int mes) {
        return ResponseEntity.ok(reportService.ingresosMensuales(anio, mes));
    }

    @GetMapping("/ocupacion")
    public ResponseEntity<ReportResponse> ocupacion(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        return ResponseEntity.ok(reportService.ocupacion(fechaInicio, fechaFin));
    }

    @GetMapping("/reservas")
    public ResponseEntity<ReportResponse> reservasPorRango(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        return ResponseEntity.ok(reportService.reservasPorRango(fechaInicio, fechaFin));
    }

    @GetMapping("/clientes-frecuentes")
    public ResponseEntity<ReportResponse> clientesFrecuentes(
            @RequestParam(defaultValue = "10") int limite) {
        return ResponseEntity.ok(reportService.clientesFrecuentes(limite));
    }

    @GetMapping("/pagos-parciales-vs-completos")
    public ResponseEntity<ReportResponse> pagosParcialesVsCompletos(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        return ResponseEntity.ok(reportService.pagosParcialesVsCompletos(fechaInicio, fechaFin));
    }
}
