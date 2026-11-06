package com.adso1651.tienda.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Creación de usuarios por el ADMIN (acá sí se elige el rol).
 */
@Data
public class CreateUserRequestDTO {
    @NotBlank(message = "El username es obligatorio")
    @Size(max = 50, message = "El username admite máximo 50 caracteres")
    private String username;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no tiene un formato válido")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener mínimo 6 caracteres")
    private String password;

    @NotNull(message = "El rolId es obligatorio")
    private Long rolId;
}
