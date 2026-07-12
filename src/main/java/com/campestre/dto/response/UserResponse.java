package com.campestre.dto.response;

import com.campestre.enums.RoleType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponse {
    private Long id;
    private String username;
    private String nombreCompleto;
    private String email;
    private RoleType role;
    private Boolean activo;
}
