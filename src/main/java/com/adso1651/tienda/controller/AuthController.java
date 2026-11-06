package com.adso1651.tienda.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.adso1651.tienda.dto.HttpGlobalResponseDTO;
import com.adso1651.tienda.dto.LoginRequestDTO;
import com.adso1651.tienda.dto.LoginResponseDTO;
import com.adso1651.tienda.dto.RegisterRequestDTO;
import com.adso1651.tienda.dto.UserDTO;
import com.adso1651.tienda.exception.ApiException;
import com.adso1651.tienda.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<HttpGlobalResponseDTO<UserDTO>> register(@Valid @RequestBody RegisterRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<HttpGlobalResponseDTO<LoginResponseDTO>> login(@Valid @RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refreshtoken")
    public ResponseEntity<HttpGlobalResponseDTO<LoginResponseDTO>> refreshToken(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        // /auth/** es público para el filtro, así que acá se valida el header a mano
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw ApiException.unauthorized("Falta el header Authorization con el token Bearer");
        }

        String token = authHeader.substring("Bearer ".length()).trim();
        return ResponseEntity.ok(authService.refreshToken(token));
    }
}
