package com.adso1651.tienda.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.adso1651.tienda.dto.CategoryDTO;
import com.adso1651.tienda.dto.CategoryRequestDTO;
import com.adso1651.tienda.dto.HttpGlobalResponseDTO;
import com.adso1651.tienda.entity.Categories;
import com.adso1651.tienda.exception.ApiException;
import com.adso1651.tienda.repository.CategoriesRepository;
import com.adso1651.tienda.repository.ProductsRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoriesService {

    private final CategoriesRepository categoriesRepository;
    private final ProductsRepository productsRepository;

    private static CategoryDTO toDTO(Categories category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        return dto;
    }

    public HttpGlobalResponseDTO<List<CategoryDTO>> getCategories() {
        List<CategoryDTO> list = categoriesRepository.findAll().stream().map(CategoriesService::toDTO).toList();
        return HttpGlobalResponseDTO.of("Categorías encontradas", list);
    }

    public HttpGlobalResponseDTO<CategoryDTO> create(CategoryRequestDTO request) {
        String name = request.getName().trim();

        if (categoriesRepository.existsByName(name)) {
            throw ApiException.conflict("Ya existe una categoría con ese nombre");
        }

        Categories category = new Categories();
        category.setName(name);
        categoriesRepository.save(category);

        return HttpGlobalResponseDTO.of("Categoría creada correctamente", toDTO(category));
    }

    public HttpGlobalResponseDTO<Void> delete(Long id) {
        Categories category = categoriesRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("La categoría no existe"));

        if (productsRepository.existsByCategoryId(id)) {
            throw ApiException.conflict("No se puede eliminar: la categoría tiene productos");
        }

        categoriesRepository.delete(category);
        return HttpGlobalResponseDTO.of("Categoría eliminada correctamente", null);
    }
}
