package com.campestre.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class ReservationRequest {
    @NotNull(message = "El cliente es obligatorio")
    private Long clientId;

    @NotNull(message = "El ambiente es obligatorio")
    private Long environmentId;

    @NotNull(message = "La fecha del evento es obligatoria")
    private LocalDate fechaEvento;

    private LocalTime horaInicio;

    private LocalTime horaFin;

    private String notas;

    @NotNull(message = "El precio total es obligatorio")
    private BigDecimal precioTotal;

    private Boolean adelantoRequerido;

    private String tipoEvento;

    private BigDecimal precioSillas;

    private BigDecimal precioMotor;
}
