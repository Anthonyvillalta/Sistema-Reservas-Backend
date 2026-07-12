package com.campestre.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DisponibilidadResponse {
    private Long environmentId;
    private String ambienteNombre;
    private String fecha;
    private boolean disponible;
    private String mensaje;
    private List<String> horariosDisponibles;
    private List<String> horariosOcupados;
}
