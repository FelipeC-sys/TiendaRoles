package com.adso1651.tienda.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OrderStatusRequestDTO {
    /**
     * PENDIENTE, ENVIADO, ENTREGADO o CANCELADO
     */
    @NotBlank(message = "El estado es obligatorio")
    private String status;
}
