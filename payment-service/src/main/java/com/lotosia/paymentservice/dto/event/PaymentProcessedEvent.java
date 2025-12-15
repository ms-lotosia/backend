package com.lotosia.paymentservice.dto.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentProcessedEvent {
    private Long paymentId;
    private Long userId;
    private Long orderId;
    private BigDecimal amount;
    private String paymentMethod;  // Changed to String to avoid cross-service dependency
    private String status;  // Changed to String to avoid cross-service dependency
    private String transactionId;
    private LocalDateTime processedAt;
}
