package com.adso1651.tienda.filter;

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.adso1651.tienda.security.SecurityResponseWriter;
import com.adso1651.tienda.service.JwtService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtValidationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            SecurityResponseWriter.write(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "Falta el header Authorization con el token Bearer");
            return;
        }

        String token = authHeader.substring("Bearer ".length()).trim();

        Claims claims;
        try {
            claims = jwtService.parseClaims(token);
        } catch (ExpiredJwtException e) {
            SecurityResponseWriter.write(response, HttpServletResponse.SC_UNAUTHORIZED, "El token está expirado");
            return;
        } catch (JwtException | IllegalArgumentException e) {
            SecurityResponseWriter.write(response, HttpServletResponse.SC_UNAUTHORIZED, "El token es inválido");
            return;
        }

        Long userId = jwtService.extractUserId(claims);
        Long rolId = jwtService.extractRolId(claims);

        if (userId == null || rolId == null) {
            SecurityResponseWriter.write(response, HttpServletResponse.SC_UNAUTHORIZED, "El token es inválido");
            return;
        }

        // Atributos que usarán el interceptor (rolId) y los controllers / servicios (userId, rolId)
        request.setAttribute("username", jwtService.extractUsername(claims));
        request.setAttribute("userId", userId);
        request.setAttribute("rolId", rolId);

        // Fuera del try: un error del controller NO debe convertirse en un 401
        filterChain.doFilter(request, response);
    }

    /**
     * Rutas públicas (el filtro no exige token):
     *  - todo /auth/**
     *  - GET /products y GET /products/{id}  (OJO: GET /products/mine NO es público)
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // getRequestURI incluye el context-path (/api/v1/tienda); se lo quitamos para comparar
        String path = request.getRequestURI().substring(request.getContextPath().length());
        String method = request.getMethod();

        if (path.equals("/auth") || path.startsWith("/auth/")) {
            return true;
        }

        if ("GET".equalsIgnoreCase(method)) {
            boolean catalog = path.equals("/products") || path.equals("/products/");
            boolean detail = path.matches("/products/\\d+/?");
            return catalog || detail;
        }

        return false;
    }
}
