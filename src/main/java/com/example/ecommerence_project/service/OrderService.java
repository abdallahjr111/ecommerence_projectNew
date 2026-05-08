package com.example.ecommerence_project.service;

import com.example.ecommerence_project.dto.request.CheckoutRequest;
import com.example.ecommerence_project.dto.request.OrderStatusUpdateRequest;
import com.example.ecommerence_project.dto.response.OrderResponse;

import java.util.List;

public interface OrderService {

    OrderResponse checkout(CheckoutRequest request, String email);

    List<OrderResponse> getMyOrders(String email);

    OrderResponse getOrderById(Long orderId, String email);

    // Admin operations
    List<OrderResponse> getAllOrders();

    OrderResponse updateOrderStatus(Long orderId, OrderStatusUpdateRequest request);
}
