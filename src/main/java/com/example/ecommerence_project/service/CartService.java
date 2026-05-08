package com.example.ecommerence_project.service;

import com.example.ecommerence_project.dto.request.CartItemRequest;
import com.example.ecommerence_project.dto.response.CartResponse;

public interface CartService {

    CartResponse getCart(String email);

    CartResponse addItem(CartItemRequest request, String email);

    CartResponse updateItem(Long cartItemId, Integer quantity, String email);

    CartResponse removeItem(Long cartItemId, String email);

    void clearCart(String email);
}
