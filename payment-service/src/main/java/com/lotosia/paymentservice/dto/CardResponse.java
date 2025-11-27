package com.lotosia.paymentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardResponse {

    private Long id;
    private Long userId;
    private String maskedCardNumber;
    private String cardHolderName;
    private Integer expiryMonth;
    private Integer expiryYear;
    private Boolean isDefault;
    private Boolean isActive;
    private LocalDateTime createdDate;
}


