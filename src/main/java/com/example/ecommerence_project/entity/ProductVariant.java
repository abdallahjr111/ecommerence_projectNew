package com.example.ecommerence_project.entity;

import com.example.ecommerence_project.enums.GenderType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "product_variants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /** Volume in ml, e.g. 30, 50, 100 */
    @Column(nullable = false)
    private Integer sizeInMl;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    @Builder.Default
    private Integer stockQuantity = 0;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private GenderType genderType;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;
}
