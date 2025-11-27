package com.lotosia.paymentservice.controller;

import com.lotosia.paymentservice.dto.PaymentRequest;
import com.lotosia.paymentservice.dto.PaymentResponse;
import com.lotosia.paymentservice.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springdoc.core.annotations.ParameterObject;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payment Controller")
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "Process a payment")
    @PostMapping
    public ResponseEntity<PaymentResponse> processPayment(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody PaymentRequest request) {
        PaymentResponse response = paymentService.processPayment(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get payment by ID")
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPayment(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long paymentId) {
        PaymentResponse payment = paymentService.getPayment(userId, paymentId);
        return ResponseEntity.ok(payment);
    }

    @Operation(summary = "Get all user payments")
    @GetMapping
    public ResponseEntity<Page<PaymentResponse>> getUserPayments(
            @RequestHeader("X-User-Id") Long userId,
            @ParameterObject @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        Page<PaymentResponse> payments = paymentService.getUserPayments(userId, pageable);
        return ResponseEntity.ok(payments);
    }

    @Operation(summary = "Get payments by order ID")
    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<PaymentResponse>> getOrderPayments(@PathVariable Long orderId) {
        List<PaymentResponse> payments = paymentService.getOrderPayments(orderId);
        return ResponseEntity.ok(payments);
    }

    @Operation(summary = "Cancel a payment")
    @PutMapping("/{paymentId}/cancel")
    public ResponseEntity<PaymentResponse> cancelPayment(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long paymentId) {
        PaymentResponse payment = paymentService.cancelPayment(userId, paymentId);
        return ResponseEntity.ok(payment);
    }
}


