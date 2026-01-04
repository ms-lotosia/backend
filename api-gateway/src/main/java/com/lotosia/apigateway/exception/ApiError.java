package com.lotosia.apigateway.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiError {
    private String code;
    private String message;
    private int status;
    private String path;
    @Builder.Default
    private long timestamp = System.currentTimeMillis();
}
