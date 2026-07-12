package com.campestre.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ClientResponse {
    private Long id;
    private String nombre;
    private String celular;
    private String email;
    private String documentoIdentidad;
    private String direccion;
    private LocalDateTime createdAt;
    private long totalReservas;
    private List<ReservationSummaryResponse> ultimasReservas;
}
