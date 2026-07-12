package com.campestre.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class ReservationSummaryResponse {
    private Long id;
    private String codigoReserva;
    private String ambienteNombre;
    private LocalDate fechaEvento;
    private String estado;
    private BigDecimal precioTotal;
    private String estadoPago;
}
