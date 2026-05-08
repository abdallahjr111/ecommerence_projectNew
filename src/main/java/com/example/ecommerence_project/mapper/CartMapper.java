package com.example.ecommerence_project.mapper;

import com.example.ecommerence_project.dto.response.CartItemResponse;
import com.example.ecommerence_project.dto.response.CartResponse;
import com.example.ecommerence_project.entity.Cart;
import com.example.ecommerence_project.entity.CartItem;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CartMapper {

    public CartResponse toResponse(Cart cart) {
        List<CartItemResponse> itemResponses = cart.getItems() == null ? List.of() :
                cart.getItems().stream()
                        .map(this::toCartItemResponse)
                        .collect(Collectors.toList());

        BigDecimal total = itemResponses.stream()
                .map(CartItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder()
                .id(cart.getId())
                .userId(cart.getUser().getId())
                .items(itemResponses)
                .totalAmount(total)
                .build();
    }

    public CartItemResponse toCartItemResponse(CartItem item) {
        String productName = item.getProductVariant() != null && item.getProductVariant().getProduct() != null
                ? item.getProductVariant().getProduct().getName()
                : null;
        Integer sizeInMl = item.getProductVariant() != null
                ? item.getProductVariant().getSizeInMl()
                : null;
        BigDecimal subtotal = item.getUnitPrice() != null && item.getQuantity() != null
                ? item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()))
                : BigDecimal.ZERO;

        return CartItemResponse.builder()
                .id(item.getId())
                .productVariantId(item.getProductVariant() != null ? item.getProductVariant().getId() : null)
                .productName(productName)
                .sizeInMl(sizeInMl)
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .subtotal(subtotal)
                .build();
    }
}
