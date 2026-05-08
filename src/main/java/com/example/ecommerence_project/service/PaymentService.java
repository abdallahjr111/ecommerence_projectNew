package com.example.ecommerence_project.service;

import com.example.ecommerence_project.dto.response.PaymentResponse;
import com.example.ecommerence_project.entity.Order;
import com.example.ecommerence_project.enums.PaymentMethod;

public interface PaymentService {
    PaymentResponse processPayment(Order order, PaymentMethod paymentMethod);
    PaymentResponse getPaymentByOrderId(Long orderId);
}
