package com.example.ecommerence_project.service;

import com.example.ecommerence_project.dto.request.RefundRequestDto;
import com.example.ecommerence_project.dto.response.RefundResponse;
import com.example.ecommerence_project.enums.RefundStatus;

import java.util.List;

public interface RefundService {

    RefundResponse requestRefund(RefundRequestDto request, String email);

    List<RefundResponse> getMyRefunds(String email);

    // Admin operations
    List<RefundResponse> getAllRefunds();

    List<RefundResponse> getRefundsByStatus(RefundStatus status);

    RefundResponse updateRefundStatus(Long refundId, RefundStatus status, String adminNote);
}
