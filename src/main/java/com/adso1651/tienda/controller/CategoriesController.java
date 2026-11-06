package com.adso1651.tienda.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.adso1651.tienda.dto.CategoryDTO;
import com.adso1651.tienda.dto.CategoryRequestDTO;
import com.adso1651.tienda.dto.HttpGlobalResponseDTO;
import com.adso1651.tienda.enums.RoleEnum;
import com.adso1651.tienda.security.RequiresRole;
import com.adso1651.tienda.service.CategoriesService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoriesController {

    private final CategoriesService categoriesService;

    @GetMapping
    @RequiresRole({ RoleEnum.ADMIN, RoleEnum.CLIENTE, RoleEnum.VENDEDOR })
    public ResponseEntity<HttpGlobalResponseDTO<List<CategoryDTO>>> getCategories() {
        return ResponseEntity.ok(categoriesService.getCategories());
    }

    @PostMapping
    @RequiresRole(RoleEnum.ADMIN)
    public ResponseEntity<HttpGlobalResponseDTO<CategoryDTO>> create(@Valid @RequestBody CategoryRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoriesService.create(request));
    }

    @DeleteMapping("/{id}")
    @RequiresRole(RoleEnum.ADMIN)
    public ResponseEntity<HttpGlobalResponseDTO<Void>> delete(@PathVariable Long id) {
        return ResponseEntity.ok(categoriesService.delete(id));
    }
}
