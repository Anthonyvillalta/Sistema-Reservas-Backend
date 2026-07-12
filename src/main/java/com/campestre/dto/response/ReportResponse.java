package com.campestre.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
public class ReportResponse {
    private String tipo;
    private Map<String, Object> datos;
}
