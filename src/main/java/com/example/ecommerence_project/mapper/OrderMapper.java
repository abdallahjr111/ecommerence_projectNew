package com.example.ecommerence_project.mapper;

import com.example.ecommerence_project.dto.response.OrderItemResponse;
import com.example.ecommerence_project.dto.response.OrderResponse;
import com.example.ecommerence_project.dto.response.PaymentResponse;
import com.example.ecommerence_project.entity.Order;
import com.example.ecommerence_project.entity.OrderItem;
import com.example.ecommerence_project.entity.Payment;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderMapper {

    public OrderResponse toResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems() == null ? List.of() :
                order.getItems().stream()
                        .map(this::toOrderItemResponse)
                        .collect(Collectors.toList());

        PaymentResponse paymentResponse = order.getPayment() != null
                ? toPaymentResponse(order.getPayment())
                : null;

        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUser().getId())
                .userFullName(order.getUser().getFullName())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .shippingAddress(order.getShippingAddress())
                .items(itemResponses)
                .payment(paymentResponse)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    public OrderItemResponse toOrderItemResponse(OrderItem item) {
        String productName = item.getProductVariant() != null && item.getProductVariant().getProduct() != null
                ? item.getProductVariant().getProduct().getName()
                : null;
        Integer sizeInMl = item.getProductVariant() != null
                ? item.getProductVariant().getSizeInMl()
                : null;

        return OrderItemResponse.builder()
                .id(item.getId())
                .productVariantId(item.getProductVariant() != null ? item.getProductVariant().getId() : null)
                .productName(productName)
                .sizeInMl(sizeInMl)
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .subtotal(item.getSubtotal())
                .build();
    }

    public PaymentResponse toPaymentResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .orderId(payment.getOrder() != null ? payment.getOrder().getId() : null)
                .paymentMethod(payment.getPaymentMethod())
                .paymentStatus(payment.getPaymentStatus())
                .amount(payment.getAmount())
                .transactionReference(payment.getTransactionReference())
                .processedAt(payment.getProcessedAt())
                .build();
    }
}
