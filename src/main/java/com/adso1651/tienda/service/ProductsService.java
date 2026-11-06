package com.adso1651.tienda.service;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.adso1651.tienda.dto.HttpGlobalResponseDTO;
import com.adso1651.tienda.dto.ProductDTO;
import com.adso1651.tienda.dto.ProductRequestDTO;
import com.adso1651.tienda.entity.Categories;
import com.adso1651.tienda.entity.Products;
import com.adso1651.tienda.entity.Users;
import com.adso1651.tienda.enums.RoleEnum;
import com.adso1651.tienda.exception.ApiException;
import com.adso1651.tienda.repository.CategoriesRepository;
import com.adso1651.tienda.repository.OrdersRepository;
import com.adso1651.tienda.repository.ProductsRepository;
import com.adso1651.tienda.repository.UsersRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductsService {

    private final ProductsRepository productsRepository;
    private final CategoriesRepository categoriesRepository;
    private final UsersRepository usersRepository;
    private final OrdersRepository ordersRepository;

    private static ProductDTO toDTO(Products product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setStock(product.getStock());
        dto.setCategoryId(product.getCategory().getId());
        dto.setCategoryName(product.getCategory().getName());
        dto.setSellerId(product.getSeller().getId());
        dto.setSellerUsername(product.getSeller().getUsername());
        return dto;
    }

    /**
     * Catálogo público. Filtro opcional por categoría y paginación opcional (?page=&size=).
     */
    public HttpGlobalResponseDTO<List<ProductDTO>> getProducts(Long categoryId, Integer page, Integer size) {
        List<Products> products;

        if (page == null && size == null) {
            products = categoryId == null ? productsRepository.findAll() : productsRepository.findByCategoryId(categoryId);
        } else {
            int pageNumber = page == null ? 0 : page;
            int pageSize = size == null ? 10 : size;
            if (pageNumber < 0 || pageSize < 1 || pageSize > 100) {
                throw ApiException.badRequest("page debe ser >= 0 y size debe estar entre 1 y 100");
            }
            Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("id"));
            products = categoryId == null
                    ? productsRepository.findAll(pageable).getContent()
                    : productsRepository.findByCategoryId(categoryId, pageable);
        }

        return HttpGlobalResponseDTO.of("Productos encontrados", products.stream().map(ProductsService::toDTO).toList());
    }

    public HttpGlobalResponseDTO<ProductDTO> getById(Long id) {
        return HttpGlobalResponseDTO.of("Producto encontrado", toDTO(findProduct(id)));
    }

    public HttpGlobalResponseDTO<List<ProductDTO>> getMine(Long sellerId) {
        List<ProductDTO> list = productsRepository.findBySellerId(sellerId).stream().map(ProductsService::toDTO).toList();
        return HttpGlobalResponseDTO.of("Tus productos", list);
    }

    public HttpGlobalResponseDTO<ProductDTO> create(ProductRequestDTO request, Long sellerId) {
        // El dueño sale del token (userId), jamás del body
        Users seller = usersRepository.findById(sellerId)
                .orElseThrow(() -> ApiException.unauthorized("El usuario del token ya no existe"));
        Categories category = findCategory(request.getCategoryId());

        Products product = new Products();
        applyRequest(product, request, category);
        product.setSeller(seller);
        productsRepository.save(product);

        return HttpGlobalResponseDTO.of("Producto creado correctamente", toDTO(product));
    }

    public HttpGlobalResponseDTO<ProductDTO> update(Long id, ProductRequestDTO request, Long userId, Long rolId) {
        Products product = findProduct(id);
        checkOwnership(product, userId, rolId);
        Categories category = findCategory(request.getCategoryId());

        applyRequest(product, request, category);
        productsRepository.save(product);

        return HttpGlobalResponseDTO.of("Producto actualizado correctamente", toDTO(product));
    }

    public HttpGlobalResponseDTO<Void> delete(Long id, Long userId, Long rolId) {
        Products product = findProduct(id);
        checkOwnership(product, userId, rolId);

        if (ordersRepository.existsByProductId(id)) {
            throw ApiException.conflict("No se puede eliminar: el producto tiene pedidos asociados");
        }

        productsRepository.delete(product);
        return HttpGlobalResponseDTO.of("Producto eliminado correctamente", null);
    }

    // ---------- helpers ----------

    private Products findProduct(Long id) {
        return productsRepository.findById(id).orElseThrow(() -> ApiException.notFound("El producto no existe"));
    }

    private Categories findCategory(Long categoryId) {
        return categoriesRepository.findById(categoryId).orElseThrow(() -> ApiException.notFound("La categoría no existe"));
    }

    private void applyRequest(Products product, ProductRequestDTO request, Categories category) {
        product.setName(request.getName().trim());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setCategory(category);
    }

    /**
     * Regla de propiedad: el ADMIN puede todo; el VENDEDOR solo sobre sus productos (si no → 403).
     */
    private void checkOwnership(Products product, Long userId, Long rolId) {
        boolean isAdmin = RoleEnum.ADMIN.getId().equals(rolId);
        boolean isOwner = product.getSeller().getId().equals(userId);

        if (!isAdmin && !isOwner) {
            throw ApiException.forbidden("No puedes modificar un producto que no es tuyo");
        }
    }
}
