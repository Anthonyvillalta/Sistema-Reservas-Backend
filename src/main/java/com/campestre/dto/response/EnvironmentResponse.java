package com.campestre.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class EnvironmentResponse {
    private Long id;
    private String nombre;
    private String tipo;
    private String descripcion;
    private BigDecimal precioBase;
    private Integer capacidadMaxima;
    private String estado;
    private String imagenUrl;
    private LocalDateTime createdAt;
}
