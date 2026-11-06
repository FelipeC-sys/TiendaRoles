package com.adso1651.tienda.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HttpGlobalResponseDTO<T> {
    /**
     * Mensaje de respuesta
     */
    private String message;

    /**
     * Objeto de respuesta
     */
    private T data;

    public static <T> HttpGlobalResponseDTO<T> of(String message, T data) {
        return new HttpGlobalResponseDTO<>(message, data);
    }
}
