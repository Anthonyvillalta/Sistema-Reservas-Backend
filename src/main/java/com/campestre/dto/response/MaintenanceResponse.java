package com.campestre.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MaintenanceResponse {
    private Long id;
    private Long environmentId;
    private String ambienteNombre;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private String motivo;
    private String estado;
    private String creadoPor;
    private LocalDateTime createdAt;
}
