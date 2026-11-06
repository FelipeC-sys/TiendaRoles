package com.adso1651.tienda.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class OrderDTO {
    private Long id;
    private Long clientId;
    private String clientUsername;
    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal total;
    private String status;
    private LocalDateTime createdAt;
}
