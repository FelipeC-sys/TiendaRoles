package com.adso1651.tienda.security;

import java.io.IOException;

import jakarta.servlet.http.HttpServletResponse;

/**
 * Escribe respuestas de error con la misma forma de HttpGlobalResponseDTO ({"message": ..., "data": null})
 * desde el filtro y el interceptor, que corren antes de llegar al controller / advice.
 */
public final class SecurityResponseWriter {

    private SecurityResponseWriter() {
    }

    public static void write(HttpServletResponse response, int status, String message) throws IOException {
        String safe = message.replace("\\", "\\\\").replace("\"", "\\\"");
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"message\":\"" + safe + "\",\"data\":null}");
    }
}
