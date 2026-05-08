package com.example.ecommerence_project.service.impl;

import com.example.ecommerence_project.dto.request.RefundRequestDto;
import com.example.ecommerence_project.dto.response.RefundResponse;
import com.example.ecommerence_project.entity.Order;
import com.example.ecommerence_project.entity.RefundRequest;
import com.example.ecommerence_project.entity.User;
import com.example.ecommerence_project.enums.OrderStatus;
import com.example.ecommerence_project.enums.RefundStatus;
import com.example.ecommerence_project.exception.BadRequestException;
import com.example.ecommerence_project.exception.ResourceNotFoundException;
import com.example.ecommerence_project.exception.UnauthorizedException;
import com.example.ecommerence_project.mapper.RefundMapper;
import com.example.ecommerence_project.repository.OrderRepository;
import com.example.ecommerence_project.repository.RefundRequestRepository;
import com.example.ecommerence_project.repository.UserRepository;
import com.example.ecommerence_project.service.RefundService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RefundServiceImpl implements RefundService {

    private final RefundRequestRepository refundRequestRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final RefundMapper refundMapper;

    @Override
    public RefundResponse requestRefund(RefundRequestDto request, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + request.getOrderId()));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You do not have access to this order");
        }

        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new BadRequestException("Refunds can only be requested for delivered orders");
        }

        boolean alreadyRequested = !refundRequestRepository.findByOrderId(order.getId()).isEmpty();
        if (alreadyRequested) {
            throw new BadRequestException("A refund request already exists for this order");
        }

        RefundRequest refund = RefundRequest.builder()
                .order(order)
                .user(user)
                .reason(request.getReason())
                .build();

        return refundMapper.toResponse(refundRequestRepository.save(refund));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RefundResponse> getMyRefunds(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return refundRequestRepository.findByUserId(user.getId()).stream()
                .map(refundMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RefundResponse> getAllRefunds() {
        return refundRequestRepository.findAll().stream()
                .map(refundMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RefundResponse> getRefundsByStatus(RefundStatus status) {
        return refundRequestRepository.findByStatus(status).stream()
                .map(refundMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public RefundResponse updateRefundStatus(Long refundId, RefundStatus status, String adminNote) {
        RefundRequest refund = refundRequestRepository.findById(refundId)
                .orElseThrow(() -> new ResourceNotFoundException("Refund request not found with id: " + refundId));

        refund.setStatus(status);
        if (adminNote != null) {
            refund.setAdminNote(adminNote);
        }

        // If approved, mark the order as refunded
        if (status == RefundStatus.APPROVED || status == RefundStatus.COMPLETED) {
            refund.getOrder().setStatus(OrderStatus.REFUNDED);
            orderRepository.save(refund.getOrder());
        }

        return refundMapper.toResponse(refundRequestRepository.save(refund));
    }
}
