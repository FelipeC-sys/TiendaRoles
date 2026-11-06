package com.adso1651.tienda.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Crear/editar producto. NO trae sellerId: el dueño sale del token.
 */
@Data
public class ProductRequestDTO {
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 150, message = "El nombre admite máximo 150 caracteres")
    private String name;

    @Size(max = 500, message = "La descripción admite máximo 500 caracteres")
    private String description;

    @NotNull(message = "El precio es obligatorio")
    @Positive(message = "El precio debe ser mayor a 0")
    private BigDecimal price;

    @NotNull(message = "El stock es obligatorio")
    @PositiveOrZero(message = "El stock no puede ser negativo")
    private Integer stock;

    @NotNull(message = "La categoría es obligatoria")
    private Long categoryId;
}
