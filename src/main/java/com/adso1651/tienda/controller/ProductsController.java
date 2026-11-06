package com.adso1651.tienda.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.adso1651.tienda.dto.HttpGlobalResponseDTO;
import com.adso1651.tienda.dto.ProductDTO;
import com.adso1651.tienda.dto.ProductRequestDTO;
import com.adso1651.tienda.enums.RoleEnum;
import com.adso1651.tienda.security.RequiresRole;
import com.adso1651.tienda.service.ProductsService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductsController {

    private final ProductsService productsService;

    // ----- Públicos (sin @RequiresRole y excluidos en el filtro) -----

    @GetMapping
    public ResponseEntity<HttpGlobalResponseDTO<List<ProductDTO>>> getProducts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return ResponseEntity.ok(productsService.getProducts(categoryId, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<HttpGlobalResponseDTO<ProductDTO>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(productsService.getById(id));
    }

    // ----- Protegidos -----

    @GetMapping("/mine")
    @RequiresRole(RoleEnum.VENDEDOR)
    public ResponseEntity<HttpGlobalResponseDTO<List<ProductDTO>>> getMine(@RequestAttribute("userId") Long userId) {
        return ResponseEntity.ok(productsService.getMine(userId));
    }

    @PostMapping
    @RequiresRole(RoleEnum.VENDEDOR)
    public ResponseEntity<HttpGlobalResponseDTO<ProductDTO>> create(@Valid @RequestBody ProductRequestDTO request,
            @RequestAttribute("userId") Long userId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productsService.create(request, userId));
    }

    @PutMapping("/{id}")
    @RequiresRole({ RoleEnum.VENDEDOR, RoleEnum.ADMIN })
    public ResponseEntity<HttpGlobalResponseDTO<ProductDTO>> update(@PathVariable Long id,
            @Valid @RequestBody ProductRequestDTO request,
            @RequestAttribute("userId") Long userId,
            @RequestAttribute("rolId") Long rolId) {
        return ResponseEntity.ok(productsService.update(id, request, userId, rolId));
    }

    @DeleteMapping("/{id}")
    @RequiresRole({ RoleEnum.VENDEDOR, RoleEnum.ADMIN })
    public ResponseEntity<HttpGlobalResponseDTO<Void>> delete(@PathVariable Long id,
            @RequestAttribute("userId") Long userId,
            @RequestAttribute("rolId") Long rolId) {
        return ResponseEntity.ok(productsService.delete(id, userId, rolId));
    }
}
