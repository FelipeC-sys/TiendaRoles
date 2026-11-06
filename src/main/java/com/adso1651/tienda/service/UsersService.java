package com.adso1651.tienda.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.adso1651.tienda.dto.CreateUserRequestDTO;
import com.adso1651.tienda.dto.HttpGlobalResponseDTO;
import com.adso1651.tienda.dto.UpdateRoleRequestDTO;
import com.adso1651.tienda.dto.UpdateUserStatusRequestDTO;
import com.adso1651.tienda.dto.UserDTO;
import com.adso1651.tienda.entity.Roles;
import com.adso1651.tienda.entity.Users;
import com.adso1651.tienda.exception.ApiException;
import com.adso1651.tienda.repository.RolesRepository;
import com.adso1651.tienda.repository.UsersRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsersService {

    private final UsersRepository usersRepository;
    private final RolesRepository rolesRepository;
    private final PasswordEncoder passwordEncoder;

    /** Convierte la entidad en DTO. El DTO no tiene password, así que nunca sale en una respuesta. */
    public static UserDTO toDTO(Users user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setActive(user.getActive());
        dto.setRolId(user.getRoles().getId());
        dto.setRol(user.getRoles().getName());
        return dto;
    }

    public HttpGlobalResponseDTO<UserDTO> getMe(Long userId) {
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("El usuario no existe"));
        return HttpGlobalResponseDTO.of("Perfil del usuario", toDTO(user));
    }

    public HttpGlobalResponseDTO<List<UserDTO>> getUsers() {
        List<UserDTO> users = usersRepository.findAll().stream().map(UsersService::toDTO).toList();
        return HttpGlobalResponseDTO.of("Usuarios encontrados", users);
    }

    public HttpGlobalResponseDTO<UserDTO> createUser(CreateUserRequestDTO request) {
        String username = request.getUsername().trim();
        String email = request.getEmail().trim();

        if (usersRepository.existsByUsername(username)) {
            throw ApiException.conflict("El nombre de usuario ya está en uso");
        }
        if (usersRepository.existsByEmail(email)) {
            throw ApiException.conflict("El correo ya está en uso");
        }
        Roles rol = rolesRepository.findById(request.getRolId())
                .orElseThrow(() -> ApiException.notFound("El rol no existe"));

        Users user = new Users();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setActive(true);
        user.setRoles(rol);
        usersRepository.save(user);

        return HttpGlobalResponseDTO.of("Usuario creado correctamente", toDTO(user));
    }

    public HttpGlobalResponseDTO<UserDTO> updateRole(Long id, UpdateRoleRequestDTO request) {
        Users user = usersRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("El usuario no existe"));
        Roles rol = rolesRepository.findById(request.getRolId())
                .orElseThrow(() -> ApiException.notFound("El rol no existe"));

        user.setRoles(rol);
        usersRepository.save(user);

        return HttpGlobalResponseDTO.of("Rol actualizado correctamente", toDTO(user));
    }

    public HttpGlobalResponseDTO<UserDTO> updateStatus(Long id, UpdateUserStatusRequestDTO request, Long requesterId) {
        Users user = usersRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("El usuario no existe"));

        // Evita que el único ADMIN se desactive a sí mismo y se quede sin acceso
        if (id.equals(requesterId) && !request.getActive()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "No puedes desactivar tu propio usuario");
        }

        user.setActive(request.getActive());
        usersRepository.save(user);

        return HttpGlobalResponseDTO.of(request.getActive() ? "Usuario activado" : "Usuario desactivado", toDTO(user));
    }
}
