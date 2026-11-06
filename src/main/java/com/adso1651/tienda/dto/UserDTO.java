package com.adso1651.tienda.dto;

import lombok.Data;

/**
 * Respuesta de usuario. Por diseño NO tiene campo password.
 */
@Data
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private Boolean active;
    private Long rolId;
    private String rol;
}
