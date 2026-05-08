package com.example.ecommerence_project.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse {
    private Long id;
    private Long productVariantId;
    private String productName;
    private Integer sizeInMl;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
}
