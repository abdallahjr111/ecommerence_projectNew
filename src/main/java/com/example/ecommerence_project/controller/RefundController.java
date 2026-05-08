package com.example.ecommerence_project.controller;

import com.example.ecommerence_project.dto.request.RefundRequestDto;
import com.example.ecommerence_project.dto.response.RefundResponse;
import com.example.ecommerence_project.service.RefundService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/refunds")
@RequiredArgsConstructor
@Tag(name = "Refunds", description = "Refund request management")
public class RefundController {

    private final RefundService refundService;

    @PostMapping
    @Operation(summary = "Submit a refund request for a delivered order")
    public ResponseEntity<RefundResponse> requestRefund(
            @Valid @RequestBody RefundRequestDto request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(refundService.requestRefund(request, userDetails.getUsername()));
    }

    @GetMapping("/my")
    @Operation(summary = "Get current user's refund requests")
    public ResponseEntity<List<RefundResponse>> getMyRefunds(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(refundService.getMyRefunds(userDetails.getUsername()));
    }
}
