package com.adso1651.tienda.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.adso1651.tienda.dto.HttpGlobalResponseDTO;
import com.adso1651.tienda.dto.LoginRequestDTO;
import com.adso1651.tienda.dto.LoginResponseDTO;
import com.adso1651.tienda.dto.RegisterRequestDTO;
import com.adso1651.tienda.dto.UserDTO;
import com.adso1651.tienda.entity.Roles;
import com.adso1651.tienda.entity.Users;
import com.adso1651.tienda.enums.RoleEnum;
import com.adso1651.tienda.exception.ApiException;
import com.adso1651.tienda.repository.RolesRepository;
import com.adso1651.tienda.repository.UsersRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsersRepository usersRepository;
    private final RolesRepository rolesRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    /**
     * Registro público: SIEMPRE crea un CLIENTE (el body no trae rolId).
     */
    public HttpGlobalResponseDTO<UserDTO> register(RegisterRequestDTO request) {
        String username = request.getUsername().trim();
        String email = request.getEmail().trim();

        if (usersRepository.existsByUsername(username)) {
            throw ApiException.conflict("El nombre de usuario ya está en uso");
        }
        if (usersRepository.existsByEmail(email)) {
            throw ApiException.conflict("El correo ya está en uso");
        }

        Roles rolCliente = rolesRepository.findById(RoleEnum.CLIENTE.getId())
                .orElseThrow(() -> new IllegalStateException("El rol CLIENTE no existe: ejecuta tienda.sql"));

        Users user = new Users();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword())); // BCrypt
        user.setActive(true);
        user.setRoles(rolCliente);
        usersRepository.save(user);

        return HttpGlobalResponseDTO.of("Usuario registrado correctamente", UsersService.toDTO(user));
    }

    public HttpGlobalResponseDTO<LoginResponseDTO> login(LoginRequestDTO request) {
        String identifier = request.getUser().trim();

        // Busca por username y, si no aparece, por email
        Users user = usersRepository.findByUsername(identifier)
                .or(() -> usersRepository.findByEmail(identifier))
                .orElseThrow(() -> ApiException.unauthorized("Usuario y/o contraseña incorrectos"));

        // Mismo mensaje para usuario inexistente y contraseña mala: no revela cuál de los dos falló
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw ApiException.unauthorized("Usuario y/o contraseña incorrectos");
        }

        if (!Boolean.TRUE.equals(user.getActive())) {
            throw ApiException.unauthorized("El usuario está inactivo");
        }

        return HttpGlobalResponseDTO.of("Inicio de sesión exitoso", buildLoginResponse(user));
    }

    /**
     * Recibe un token vigente y devuelve uno nuevo. Los datos (rol, estado) se leen de la base de datos,
     * así un cambio de rol o una desactivación se reflejan al refrescar.
     */
    public HttpGlobalResponseDTO<LoginResponseDTO> refreshToken(String token) {
        Claims claims;
        try {
            claims = jwtService.parseClaims(token);
        } catch (ExpiredJwtException e) {
            throw ApiException.unauthorized("El token está expirado");
        } catch (JwtException | IllegalArgumentException e) {
            throw ApiException.unauthorized("El token es inválido");
        }

        Long userId = jwtService.extractUserId(claims);
        Users user = (userId == null ? java.util.Optional.<Users>empty() : usersRepository.findById(userId))
                .orElseThrow(() -> ApiException.unauthorized("El token es inválido"));

        if (!Boolean.TRUE.equals(user.getActive())) {
            throw ApiException.unauthorized("El usuario está inactivo");
        }

        return HttpGlobalResponseDTO.of("Token refrescado correctamente", buildLoginResponse(user));
    }

    private LoginResponseDTO buildLoginResponse(Users user) {
        LoginResponseDTO login = new LoginResponseDTO();
        login.setJwt(jwtService.generateToken(user.getId(), user.getUsername(), user.getRoles().getId()));
        login.setUser(UsersService.toDTO(user));
        return login;
    }
}
