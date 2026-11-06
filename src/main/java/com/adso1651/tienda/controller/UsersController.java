package com.adso1651.tienda.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.adso1651.tienda.dto.CreateUserRequestDTO;
import com.adso1651.tienda.dto.HttpGlobalResponseDTO;
import com.adso1651.tienda.dto.UpdateRoleRequestDTO;
import com.adso1651.tienda.dto.UpdateUserStatusRequestDTO;
import com.adso1651.tienda.dto.UserDTO;
import com.adso1651.tienda.enums.RoleEnum;
import com.adso1651.tienda.security.RequiresRole;
import com.adso1651.tienda.service.UsersService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@RequiresRole(RoleEnum.ADMIN) // A nivel de clase: por defecto todo es solo ADMIN (el método puede sobrescribirlo)
public class UsersController {

    private final UsersService usersService;

    // La anotación del método tiene prioridad sobre la de la clase
    @GetMapping("/me")
    @RequiresRole({ RoleEnum.ADMIN, RoleEnum.CLIENTE, RoleEnum.VENDEDOR })
    public ResponseEntity<HttpGlobalResponseDTO<UserDTO>> me(@RequestAttribute("userId") Long userId) {
        return ResponseEntity.ok(usersService.getMe(userId));
    }

    @GetMapping
    public ResponseEntity<HttpGlobalResponseDTO<List<UserDTO>>> getUsers() {
        return ResponseEntity.ok(usersService.getUsers());
    }

    @PostMapping
    public ResponseEntity<HttpGlobalResponseDTO<UserDTO>> createUser(@Valid @RequestBody CreateUserRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usersService.createUser(request));
    }

    @PatchMapping("/{id}/role")
    public ResponseEntity<HttpGlobalResponseDTO<UserDTO>> updateRole(@PathVariable Long id,
            @Valid @RequestBody UpdateRoleRequestDTO request) {
        return ResponseEntity.ok(usersService.updateRole(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<HttpGlobalResponseDTO<UserDTO>> updateStatus(@PathVariable Long id,
            @Valid @RequestBody UpdateUserStatusRequestDTO request,
            @RequestAttribute("userId") Long requesterId) {
        return ResponseEntity.ok(usersService.updateStatus(id, request, requesterId));
    }
}
