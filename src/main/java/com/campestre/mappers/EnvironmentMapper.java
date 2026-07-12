package com.campestre.mappers;

import com.campestre.dto.response.EnvironmentResponse;
import com.campestre.entities.Environment;
import org.springframework.stereotype.Component;

@Component
public class EnvironmentMapper {

    public EnvironmentResponse toResponse(Environment environment) {
        return EnvironmentResponse.builder()
                .id(environment.getId())
                .nombre(environment.getNombre())
                .tipo(environment.getTipo().name())
                .descripcion(environment.getDescripcion())
                .precioBase(environment.getPrecioBase())
                .capacidadMaxima(environment.getCapacidadMaxima())
                .estado(environment.getEstado().name())
                .imagenUrl(environment.getImagenUrl())
                .createdAt(environment.getCreatedAt())
                .build();
    }
}
