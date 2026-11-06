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

import com.adso1651.tienda.dto.HttpGlobalResponseDTO;
import com.adso1651.tienda.dto.OrderDTO;
import com.adso1651.tienda.dto.OrderRequestDTO;
import com.adso1651.tienda.dto.OrderStatusRequestDTO;
import com.adso1651.tienda.enums.RoleEnum;
import com.adso1651.tienda.security.RequiresRole;
import com.adso1651.tienda.service.OrdersService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrdersController {

    private final OrdersService ordersService;

    @PostMapping
    @RequiresRole(RoleEnum.CLIENTE)
    public ResponseEntity<HttpGlobalResponseDTO<OrderDTO>> create(@Valid @RequestBody OrderRequestDTO request,
            @RequestAttribute("userId") Long userId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ordersService.create(request, userId));
    }

    @GetMapping("/mine")
    @RequiresRole(RoleEnum.CLIENTE)
    public ResponseEntity<HttpGlobalResponseDTO<List<OrderDTO>>> getMine(@RequestAttribute("userId") Long userId) {
        return ResponseEntity.ok(ordersService.getMine(userId));
    }

    @GetMapping("/sales")
    @RequiresRole(RoleEnum.VENDEDOR)
    public ResponseEntity<HttpGlobalResponseDTO<List<OrderDTO>>> getSales(@RequestAttribute("userId") Long userId) {
        return ResponseEntity.ok(ordersService.getSales(userId));
    }

    @GetMapping
    @RequiresRole(RoleEnum.ADMIN)
    public ResponseEntity<HttpGlobalResponseDTO<List<OrderDTO>>> getAll() {
        return ResponseEntity.ok(ordersService.getAll());
    }

    @PatchMapping("/{id}/status")
    @RequiresRole({ RoleEnum.VENDEDOR, RoleEnum.ADMIN })
    public ResponseEntity<HttpGlobalResponseDTO<OrderDTO>> updateStatus(@PathVariable Long id,
            @Valid @RequestBody OrderStatusRequestDTO request,
            @RequestAttribute("userId") Long userId,
            @RequestAttribute("rolId") Long rolId) {
        return ResponseEntity.ok(ordersService.updateStatus(id, request, userId, rolId));
    }

    @PatchMapping("/{id}/cancel")
    @RequiresRole(RoleEnum.CLIENTE)
    public ResponseEntity<HttpGlobalResponseDTO<OrderDTO>> cancel(@PathVariable Long id,
            @RequestAttribute("userId") Long userId) {
        return ResponseEntity.ok(ordersService.cancel(id, userId));
    }
}
