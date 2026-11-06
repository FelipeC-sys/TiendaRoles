package com.adso1651.tienda.security;

import java.util.Arrays;

import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RoleInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        // Verifica que el handler sea un método del controller y no cualquier otra cosa
        if (!(handler instanceof HandlerMethod method)) {
            return true; // Si no es un endpoint no nos importa
        }

        // 1. Busca @RequiresRole en el método; 2. si el método no la tiene, la busca en la clase
        RequiresRole annotation = method.getMethodAnnotation(RequiresRole.class);
        if (annotation == null) {
            annotation = method.getBeanType().getAnnotation(RequiresRole.class);
        }

        if (annotation == null) {
            return true; // Endpoint sin restricción de rol
        }

        Object rol = request.getAttribute("rolId");

        if (!(rol instanceof Long rolId)) {
            SecurityResponseWriter.write(response, HttpServletResponse.SC_UNAUTHORIZED, "Usuario no autenticado");
            return false; // cortamos la petición, no alcanza a llegar al controller
        }

        // Verifica si el rol está dentro de los roles permitidos.
        // OJO: se compara con equals(), no con ==, porque son objetos Long (con == solo funciona de -128 a 127).
        boolean hasRole = Arrays.stream(annotation.value()).anyMatch(role -> role.getId().equals(rolId));

        if (!hasRole) {
            SecurityResponseWriter.write(response, HttpServletResponse.SC_FORBIDDEN,
                    "No tienes permiso para realizar esta acción");
            return false;
        }

        // Si pasa todas las validaciones, permite continuar al controller
        return true;
    }
}
