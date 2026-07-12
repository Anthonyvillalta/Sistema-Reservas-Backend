package com.campestre.dto.request;

import com.campestre.enums.EnvironmentStatus;
import com.campestre.enums.EnvironmentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class EnvironmentRequest {
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotNull(message = "El tipo es obligatorio")
    private EnvironmentType tipo;

    private String descripcion;

    @NotNull(message = "El precio base es obligatorio")
    @Positive(message = "El precio debe ser positivo")
    private BigDecimal precioBase;

    @Positive(message = "La capacidad debe ser positiva")
    private Integer capacidadMaxima;

    private EnvironmentStatus estado;

    private String imagenUrl;
}
