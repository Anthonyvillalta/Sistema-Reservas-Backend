package com.campestre.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
public class ReservationResponse {
    private Long id;
    private String codigoReserva;
    private Long clientId;
    private String clienteNombre;
    private String clienteCelular;
    private String clienteEmail;
    private Long environmentId;
    private String ambienteNombre;
    private String ambienteTipo;
    private LocalDate fechaEvento;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private String estado;
    private BigDecimal precioTotal;
    private BigDecimal totalPagado;
    private BigDecimal saldoPendiente;
    private String estadoPago;
    private Boolean adelantoRequerido;
    private String notas;
    private Long createdBy;
    private String creadoPorNombre;
    private LocalDateTime createdAt;
    private List<PaymentResponse> pagos;
    private String tipoEvento;
    private BigDecimal precioSillas;
    private BigDecimal precioMotor;
}
