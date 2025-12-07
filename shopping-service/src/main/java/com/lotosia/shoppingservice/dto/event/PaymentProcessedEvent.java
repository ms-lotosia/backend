package com.lotosia.shoppingservice.dto.event;

import com.lotosia.paymentservice.enums.PaymentMethod;
import com.lotosia.paymentservice.enums.PaymentStatus;
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
    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private String transactionId;
    private LocalDateTime processedAt;
}
