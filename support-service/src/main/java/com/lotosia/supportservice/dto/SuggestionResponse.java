package com.lotosia.supportservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SuggestionResponse {
    private Long id;
    private String email;
    private String message;
    private LocalDateTime createdAt;
}
