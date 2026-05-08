package com.example.ecommerence_project.controller;

import com.example.ecommerence_project.dto.response.RefundResponse;
import com.example.ecommerence_project.enums.RefundStatus;
import com.example.ecommerence_project.service.RefundService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/refunds")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Refunds", description = "Admin refund management")
public class AdminRefundController {

    private final RefundService refundService;

    @GetMapping
    @Operation(summary = "Get all refund requests")
    public ResponseEntity<List<RefundResponse>> getAllRefunds() {
        return ResponseEntity.ok(refundService.getAllRefunds());
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get refund requests by status")
    public ResponseEntity<List<RefundResponse>> getByStatus(@PathVariable RefundStatus status) {
        return ResponseEntity.ok(refundService.getRefundsByStatus(status));
    }

    @PatchMapping("/{refundId}/status")
    @Operation(summary = "Approve or reject a refund request")
    public ResponseEntity<RefundResponse> updateStatus(
            @PathVariable Long refundId,
            @RequestParam RefundStatus status,
            @RequestParam(required = false) String adminNote) {
        return ResponseEntity.ok(refundService.updateRefundStatus(refundId, status, adminNote));
    }
}
