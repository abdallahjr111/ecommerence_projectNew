package com.example.ecommerence_project.mapper;

import com.example.ecommerence_project.dto.response.RefundResponse;
import com.example.ecommerence_project.entity.RefundRequest;
import org.springframework.stereotype.Component;

@Component
public class RefundMapper {

    public RefundResponse toResponse(RefundRequest refund) {
        return RefundResponse.builder()
                .id(refund.getId())
                .orderId(refund.getOrder().getId())
                .userId(refund.getUser().getId())
                .reason(refund.getReason())
                .status(refund.getStatus())
                .adminNote(refund.getAdminNote())
                .createdAt(refund.getCreatedAt())
                .updatedAt(refund.getUpdatedAt())
                .build();
    }
}
