package com.adso1651.tienda.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateRoleRequestDTO {
    @NotNull(message = "El rolId es obligatorio")
    private Long rolId;
}
