package com.adso1651.tienda.dto;

import lombok.Data;

@Data
public class LoginResponseDTO {
    /**
     * Token jwt generado
     */
    private String jwt;

    /**
     * Datos del usuario (sin contraseña)
     */
    private UserDTO user;
}
