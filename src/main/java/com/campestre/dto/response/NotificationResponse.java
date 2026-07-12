package com.campestre.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationResponse {
    private Long id;
    private Long reservationId;
    private String tipo;
    private String destinatario;
    private String asunto;
    private String mensaje;
    private String estadoEnvio;
    private LocalDateTime fechaEnvio;
    private LocalDateTime createdAt;
}
