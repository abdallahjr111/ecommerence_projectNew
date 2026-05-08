package com.example.ecommerence_project.service.impl;

import com.example.ecommerence_project.dto.response.PaymentResponse;
import com.example.ecommerence_project.entity.Order;
import com.example.ecommerence_project.entity.Payment;
import com.example.ecommerence_project.enums.PaymentMethod;
import com.example.ecommerence_project.enums.PaymentStatus;
import com.example.ecommerence_project.exception.ResourceNotFoundException;
import com.example.ecommerence_project.mapper.OrderMapper;
import com.example.ecommerence_project.repository.PaymentRepository;
import com.example.ecommerence_project.service.PaymentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderMapper orderMapper;

    public PaymentServiceImpl(PaymentRepository paymentRepository, OrderMapper orderMapper) {
        this.paymentRepository = paymentRepository;
        this.orderMapper = orderMapper;
    }

    @Override
    public PaymentResponse processPayment(Order order, PaymentMethod paymentMethod) {
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setPaymentMethod(paymentMethod);
        payment.setAmount(order.getTotalAmount());

        // Simulate payment processing — CASH_ON_DELIVERY stays PENDING, others complete immediately
        if (paymentMethod == PaymentMethod.CASH_ON_DELIVERY) {
            payment.setPaymentStatus(PaymentStatus.PENDING);
            payment.setTransactionReference("COD-" + order.getId());
        } else {
            payment.setPaymentStatus(PaymentStatus.COMPLETED);
            payment.setTransactionReference("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            payment.setProcessedAt(LocalDateTime.now());
        }

        Payment saved = paymentRepository.save(payment);
        return orderMapper.toPaymentResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByOrderId(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for order id: " + orderId));
        return orderMapper.toPaymentResponse(payment);
    }
}
