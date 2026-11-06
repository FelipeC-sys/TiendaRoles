package com.adso1651.tienda.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequestDTO {
    /**
     * Puede ser el username o el email del usuario
     */
    @NotBlank(message = "El usuario es obligatorio")
    private String user;

    @NotBlank(message = "La contraseña es obligatoria")
    private String password;
}
