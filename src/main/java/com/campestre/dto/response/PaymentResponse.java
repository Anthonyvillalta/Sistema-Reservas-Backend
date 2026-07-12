package com.campestre.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PaymentResponse {
    private Long id;
    private Long reservationId;
    private String codigoReserva;
    private BigDecimal monto;
    private String tipoPago;
    private String metodoPago;
    private String estado;
    private LocalDateTime fechaPago;
    private String referencia;
    private LocalDateTime createdAt;
}
