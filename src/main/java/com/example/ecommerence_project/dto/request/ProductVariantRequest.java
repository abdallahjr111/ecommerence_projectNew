package com.example.ecommerence_project.dto.request;

import com.example.ecommerence_project.enums.GenderType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductVariantRequest {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotNull(message = "Size in ml is required")
    @Min(value = 1, message = "Size must be at least 1 ml")
    private Integer sizeInMl;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal price;

    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock quantity cannot be negative")
    private Integer stockQuantity;

    private GenderType genderType;

    private Boolean active = true;
}
