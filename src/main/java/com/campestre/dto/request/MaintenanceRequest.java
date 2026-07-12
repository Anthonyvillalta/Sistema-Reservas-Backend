package com.campestre.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MaintenanceRequest {
    @NotNull(message = "El ambiente es obligatorio")
    private Long environmentId;

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDateTime fechaInicio;

    @NotNull(message = "La fecha de fin es obligatoria")
    private LocalDateTime fechaFin;

    @NotBlank(message = "El motivo es obligatorio")
    private String motivo;

    private String estado;
}
