package com.example.ecommerence_project.dto.request;

import com.example.ecommerence_project.enums.FragranceFamily;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductRequest {

    @NotBlank(message = "Product name is required")
    @Size(max = 100)
    private String name;

    @Size(max = 500)
    private String description;

    private Boolean active = true;

    @NotNull(message = "Fragrance family is required")
    private FragranceFamily fragranceFamily;

    @NotNull(message = "Category ID is required")
    private Long categoryId;
}
