package com.campestre.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiError {
    private int codigo;
    private String mensaje;
    private String detalle;
    private String ruta;
    private LocalDateTime timestamp;
    private List<String> errores;

    public static ApiError of(int codigo, String mensaje, String ruta) {
        return ApiError.builder()
                .codigo(codigo)
                .mensaje(mensaje)
                .ruta(ruta)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static ApiError of(int codigo, String mensaje, String detalle, String ruta) {
        return ApiError.builder()
                .codigo(codigo)
                .mensaje(mensaje)
                .detalle(detalle)
                .ruta(ruta)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
