package com.campestre.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CalendarEventResponse {
    private Long id;
    private String titulo;
    private String tipo; // RESERVA, MANTENIMIENTO, BLOQUEO
    private LocalDateTime inicio;
    private LocalDateTime fin;
    private String color;
    private String estado;
    private String codigoReserva;
    private String ambienteNombre;
    private String clienteNombre;
    private Boolean editable;
}
