package com.adso1651.tienda.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * Crear pedido. NO trae clientId ni total: el cliente sale del token y el total lo calcula el servidor.
 */
@Data
public class OrderRequestDTO {
    @NotNull(message = "El productId es obligatorio")
    private Long productId;

    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser mayor a 0")
    private Integer quantity;
}
