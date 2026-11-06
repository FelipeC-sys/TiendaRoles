package com.adso1651.tienda.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adso1651.tienda.dto.HttpGlobalResponseDTO;
import com.adso1651.tienda.dto.OrderDTO;
import com.adso1651.tienda.dto.OrderRequestDTO;
import com.adso1651.tienda.dto.OrderStatusRequestDTO;
import com.adso1651.tienda.entity.Orders;
import com.adso1651.tienda.entity.Products;
import com.adso1651.tienda.entity.Users;
import com.adso1651.tienda.enums.OrderStatusEnum;
import com.adso1651.tienda.enums.RoleEnum;
import com.adso1651.tienda.exception.ApiException;
import com.adso1651.tienda.repository.OrdersRepository;
import com.adso1651.tienda.repository.ProductsRepository;
import com.adso1651.tienda.repository.UsersRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrdersService {

    private final OrdersRepository ordersRepository;
    private final ProductsRepository productsRepository;
    private final UsersRepository usersRepository;

    private static OrderDTO toDTO(Orders order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setClientId(order.getClient().getId());
        dto.setClientUsername(order.getClient().getUsername());
        dto.setProductId(order.getProduct().getId());
        dto.setProductName(order.getProduct().getName());
        dto.setQuantity(order.getQuantity());
        dto.setTotal(order.getTotal());
        dto.setStatus(order.getStatus().name());
        dto.setCreatedAt(order.getCreatedAt());
        return dto;
    }

    /**
     * Crea un pedido: valida stock, calcula el total en el servidor y descuenta el stock.
     * El cliente sale del token. Todo en una transacción para que no queden datos a medias.
     */
    @Transactional
    public HttpGlobalResponseDTO<OrderDTO> create(OrderRequestDTO request, Long clientId) {
        Users client = usersRepository.findById(clientId)
                .orElseThrow(() -> ApiException.unauthorized("El usuario del token ya no existe"));

        // Bloquea la fila del producto mientras se descuenta el stock
        Products product = productsRepository.findByIdForUpdate(request.getProductId())
                .orElseThrow(() -> ApiException.notFound("El producto no existe"));

        if (product.getStock() < request.getQuantity()) {
            throw ApiException.badRequest("Stock insuficiente. Disponible: " + product.getStock());
        }

        product.setStock(product.getStock() - request.getQuantity());
        productsRepository.save(product);

        Orders order = new Orders();
        order.setClient(client);
        order.setProduct(product);
        order.setQuantity(request.getQuantity());
        order.setTotal(product.getPrice().multiply(BigDecimal.valueOf(request.getQuantity()))); // price × quantity
        order.setStatus(OrderStatusEnum.PENDIENTE);
        order.setCreatedAt(LocalDateTime.now());
        ordersRepository.save(order);

        return HttpGlobalResponseDTO.of("Pedido creado correctamente", toDTO(order));
    }

    public HttpGlobalResponseDTO<List<OrderDTO>> getMine(Long clientId) {
        List<OrderDTO> list = ordersRepository.findByClientId(clientId).stream().map(OrdersService::toDTO).toList();
        return HttpGlobalResponseDTO.of("Tus pedidos", list);
    }

    public HttpGlobalResponseDTO<List<OrderDTO>> getSales(Long sellerId) {
        List<OrderDTO> list = ordersRepository.findByProductSellerId(sellerId).stream().map(OrdersService::toDTO).toList();
        return HttpGlobalResponseDTO.of("Pedidos de tus productos", list);
    }

    public HttpGlobalResponseDTO<List<OrderDTO>> getAll() {
        List<OrderDTO> list = ordersRepository.findAll().stream().map(OrdersService::toDTO).toList();
        return HttpGlobalResponseDTO.of("Todos los pedidos", list);
    }

    /**
     * Cambia el estado. ADMIN en cualquier pedido; VENDEDOR solo en pedidos de sus productos (si no → 403).
     */
    @Transactional
    public HttpGlobalResponseDTO<OrderDTO> updateStatus(Long id, OrderStatusRequestDTO request, Long userId, Long rolId) {
        Orders order = findOrder(id);

        boolean isAdmin = RoleEnum.ADMIN.getId().equals(rolId);
        boolean isSeller = order.getProduct().getSeller().getId().equals(userId);
        if (!isAdmin && !isSeller) {
            throw ApiException.forbidden("No puedes cambiar el estado de un pedido que no es de tus productos");
        }

        OrderStatusEnum newStatus = parseStatus(request.getStatus());

        if (order.getStatus() == OrderStatusEnum.CANCELADO) {
            throw ApiException.conflict("Un pedido cancelado no puede cambiar de estado");
        }

        if (newStatus == OrderStatusEnum.CANCELADO) {
            returnStock(order); // cancelar por esta vía también devuelve el stock
        }

        order.setStatus(newStatus);
        ordersRepository.save(order);

        return HttpGlobalResponseDTO.of("Estado actualizado correctamente", toDTO(order));
    }

    /**
     * El CLIENTE cancela su propio pedido, solo si está PENDIENTE, y se devuelve el stock.
     */
    @Transactional
    public HttpGlobalResponseDTO<OrderDTO> cancel(Long id, Long clientId) {
        Orders order = findOrder(id);

        if (!order.getClient().getId().equals(clientId)) {
            throw ApiException.forbidden("No puedes cancelar un pedido que no es tuyo");
        }

        if (order.getStatus() != OrderStatusEnum.PENDIENTE) {
            throw ApiException.conflict("Solo se pueden cancelar pedidos en estado PENDIENTE");
        }

        returnStock(order);
        order.setStatus(OrderStatusEnum.CANCELADO);
        ordersRepository.save(order);

        return HttpGlobalResponseDTO.of("Pedido cancelado correctamente", toDTO(order));
    }

    // ---------- helpers ----------

    private Orders findOrder(Long id) {
        return ordersRepository.findById(id).orElseThrow(() -> ApiException.notFound("El pedido no existe"));
    }

    private OrderStatusEnum parseStatus(String value) {
        try {
            return OrderStatusEnum.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw ApiException.badRequest("Estado inválido. Valores permitidos: PENDIENTE, ENVIADO, ENTREGADO, CANCELADO");
        }
    }

    private void returnStock(Orders order) {
        Products product = productsRepository.findByIdForUpdate(order.getProduct().getId())
                .orElseThrow(() -> ApiException.notFound("El producto no existe"));
        product.setStock(product.getStock() + order.getQuantity());
        productsRepository.save(product);
    }
}
