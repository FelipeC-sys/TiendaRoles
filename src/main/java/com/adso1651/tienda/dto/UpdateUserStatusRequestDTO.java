package com.adso1651.tienda.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateUserStatusRequestDTO {
    @NotNull(message = "El campo active es obligatorio")
    private Boolean active;
}
