package com.campestre.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ClientRequest {
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no debe exceder 100 caracteres")
    private String nombre;

    @NotBlank(message = "El celular es obligatorio")
    @Size(max = 20, message = "El celular no debe exceder 20 caracteres")
    private String celular;

    @Email(message = "El email no es válido")
    private String email;

    @Size(max = 20, message = "El documento no debe exceder 20 caracteres")
    private String documentoIdentidad;

    @Size(max = 200, message = "La dirección no debe exceder 200 caracteres")
    private String direccion;
}
