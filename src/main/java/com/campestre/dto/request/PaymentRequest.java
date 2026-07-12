package com.campestre.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentRequest {
    @NotNull(message = "La reserva es obligatoria")
    private Long reservationId;

    @NotNull(message = "El monto es obligatorio")
    @Positive(message = "El monto debe ser positivo")
    private BigDecimal monto;

    @NotNull(message = "El tipo de pago es obligatorio")
    private String tipoPago;

    @NotNull(message = "El método de pago es obligatorio")
    private String metodoPago;

    private String referencia;
}
