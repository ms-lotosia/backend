package com.lotosia.identityservice.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminBootstrapResponse {
    private AdminBootstrapStatus status;
    private String message;

    public enum AdminBootstrapStatus {
        CREATED,
        UPGRADED,
        EXISTS
    }
}
