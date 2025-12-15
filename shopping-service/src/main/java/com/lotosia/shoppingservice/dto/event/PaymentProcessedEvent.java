package com.lotosia.shoppingservice.dto.event;

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
    private String paymentMethod;  
    private String status;  
    private String transactionId;
    private LocalDateTime processedAt;
}
