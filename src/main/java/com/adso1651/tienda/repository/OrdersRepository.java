package com.adso1651.tienda.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.adso1651.tienda.entity.Orders;

public interface OrdersRepository extends JpaRepository<Orders, Long> {

    List<Orders> findByClientId(Long clientId);

    /** Pedidos de los productos cuyo dueño es el vendedor indicado */
    List<Orders> findByProductSellerId(Long sellerId);

    boolean existsByProductId(Long productId);
}
