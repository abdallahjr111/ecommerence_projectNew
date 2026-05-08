package com.example.ecommerence_project.dto.response;

import com.example.ecommerence_project.enums.PaymentMethod;
import com.example.ecommerence_project.enums.PaymentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private Long id;
    private Long orderId;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private BigDecimal amount;
    private String transactionReference;
    private LocalDateTime processedAt;
}
