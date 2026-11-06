package com.adso1651.tienda.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.adso1651.tienda.entity.Products;

import jakarta.persistence.LockModeType;

public interface ProductsRepository extends JpaRepository<Products, Long> {

    List<Products> findByCategoryId(Long categoryId);

    List<Products> findByCategoryId(Long categoryId, Pageable pageable);

    List<Products> findBySellerId(Long sellerId);

    boolean existsByCategoryId(Long categoryId);

    /**
     * Busca el producto bloqueando la fila (SELECT ... FOR UPDATE) para que dos
     * pedidos simultáneos no descuenten el mismo stock.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Products p where p.id = :id")
    Optional<Products> findByIdForUpdate(@Param("id") Long id);
}
