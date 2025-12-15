package com.lotosia.paymentservice.dto;

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
public class PaymentResponse {

    private Long id;
    private Long userId;
    private Long orderId;
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private String maskedCardNumber;
    private String transactionId;
    private String description;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
}
