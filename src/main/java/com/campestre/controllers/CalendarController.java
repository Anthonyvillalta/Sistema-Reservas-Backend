package com.campestre.controllers;

import com.campestre.dto.response.CalendarEventResponse;
import com.campestre.services.CalendarService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/calendario")
public class CalendarController {

    private final CalendarService calendarService;

    public CalendarController(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    @GetMapping
    public ResponseEntity<List<CalendarEventResponse>> getEvents(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin,
            @RequestParam(required = false) Long ambienteId) {
        return ResponseEntity.ok(calendarService.getEvents(fechaInicio, fechaFin, ambienteId));
    }

    @GetMapping("/mes")
    public ResponseEntity<List<CalendarEventResponse>> getMonth(
            @RequestParam int anio,
            @RequestParam int mes,
            @RequestParam(required = false) Long ambienteId) {
        LocalDateTime inicio = LocalDateTime.of(anio, mes, 1, 0, 0);
        LocalDateTime fin = inicio.plusMonths(1).minusSeconds(1);
        return ResponseEntity.ok(calendarService.getEvents(inicio, fin, ambienteId));
    }

    @GetMapping("/semana")
    public ResponseEntity<List<CalendarEventResponse>> getWeek(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(required = false) Long ambienteId) {
        LocalDateTime inicio = fecha.atStartOfDay();
        LocalDateTime fin = fecha.plusDays(6).atTime(LocalTime.MAX);
        return ResponseEntity.ok(calendarService.getEvents(inicio, fin, ambienteId));
    }

    @GetMapping("/dia")
    public ResponseEntity<List<CalendarEventResponse>> getDay(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(required = false) Long ambienteId) {
        LocalDateTime inicio = fecha.atStartOfDay();
        LocalDateTime fin = fecha.atTime(LocalTime.MAX);
        return ResponseEntity.ok(calendarService.getEvents(inicio, fin, ambienteId));
    }
}
