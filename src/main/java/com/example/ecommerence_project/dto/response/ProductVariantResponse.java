package com.example.ecommerence_project.dto.response;

import com.example.ecommerence_project.enums.GenderType;
import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariantResponse {
    private Long id;
    private Long productId;
    private Integer sizeInMl;
    private BigDecimal price;
    private Integer stockQuantity;
    private GenderType genderType;
    private Boolean active;
}
